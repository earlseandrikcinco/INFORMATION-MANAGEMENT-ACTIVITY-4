package gui;

import app.AppController;
import app.DataAccess;
import ref.CheckerDetail;
import ref.SystemUser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Time;
import java.util.Calendar;
import java.util.List;

/**
 * Admin panel for managing checker details (schedule/shift assignments).
 *
 * Features:
 *  - View all checker detail records in a searchable table
 *  - Add a new checker detail
 *  - Edit a selected checker detail
 *  - Delete a selected checker detail (class schedules' assignedChecker → NULL safely)
 */
public class CheckerDetailsPanel extends BasePanel {

    private final DataAccess db;

    private DefaultTableModel tableModel;
    private JTable table;
    private List<CheckerDetail> currentRows;

    private JTextField searchField;

    private static final String[] DAYS = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    public CheckerDetailsPanel(AppController controller, DataAccess db) {
        super(controller);
        this.db = db;
        buildUI();
    }

    // ── main layout ──────────────────────────────────────────────────────────

    private void buildUI() {
        add(UIHelper.topBar("Checker Details", "Admin · Manage Checker Schedules"),
                BorderLayout.NORTH);

        // ── toolbar: search + action buttons ──────────────────────────────
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setBackground(UIHelper.BG);
        toolbar.setBorder(new EmptyBorder(10, 18, 6, 18));

        // Search field
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchPanel.setOpaque(false);
        JLabel searchLbl = UIHelper.sub("Search:");
        searchField = new JTextField(20);
        searchField.setFont(UIHelper.FONT_TABLE);
        JButton searchBtn = UIHelper.secondaryButton("Filter");
        searchBtn.addActionListener(e -> refreshTable(searchField.getText().trim()));
        searchField.addActionListener(e -> refreshTable(searchField.getText().trim()));
        searchPanel.add(searchLbl);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        // Action buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        JButton addBtn    = UIHelper.button("+ Add Detail");
        JButton editBtn   = UIHelper.secondaryButton("Edit");
        JButton deleteBtn = UIHelper.secondaryButton("Delete");
        editBtn.setPreferredSize(new Dimension(80, 30));
        deleteBtn.setPreferredSize(new Dimension(80, 30));
        deleteBtn.setBackground(new Color(200, 60, 60));
        deleteBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> showAddDialog());
        editBtn.addActionListener(e -> showEditDialog());
        deleteBtn.addActionListener(e -> confirmDelete());
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(btnPanel,    BorderLayout.EAST);

        // ── table ─────────────────────────────────────────────────────────
        String[] cols = {"Checker", "Schedule ID", "Day", "Shift Start", "Shift End",
                "Building", "Floor"};
        tableModel = new DefaultTableModel(cols, 0);
        table = UIHelper.makeTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(160);

        JPanel body = new JPanel(new BorderLayout(0, 4));
        body.setBackground(UIHelper.BG);
        body.setBorder(new EmptyBorder(0, 18, 10, 18));
        body.add(toolbar, BorderLayout.NORTH);
        body.add(UIHelper.scroll(table), BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
        add(bottomBar(), BorderLayout.SOUTH);

        refreshTable("");
    }

    // ── data ─────────────────────────────────────────────────────────────────

    private void refreshTable(String filter) {
        currentRows = db.getAllCheckerDetails();
        tableModel.setRowCount(0);

        String f = filter.toLowerCase();
        for (CheckerDetail cd : currentRows) {
            if (!f.isEmpty()) {
                boolean match =
                        (cd.getCheckerName() != null && cd.getCheckerName().toLowerCase().contains(f))
                                || cd.getBuilding().toLowerCase().contains(f)
                                || cd.getFloor().toLowerCase().contains(f)
                                || cd.getDay().toLowerCase().contains(f);
                if (!match) continue;
            }
            tableModel.addRow(new Object[]{
                    cd.getCheckerName() != null ? cd.getCheckerName() : "ID " + cd.getCheckerID(),
                    cd.getScheduleID(),
                    cd.getDay(),
                    cd.getShiftStart(),
                    cd.getShiftEnd(),
                    cd.getBuilding(),
                    cd.getFloor()
            });
        }
    }

    /** Returns the CheckerDetail corresponding to the currently selected table row, or null. */
    private CheckerDetail selectedDetail() {
        int row = table.getSelectedRow();
        if (row < 0 || currentRows == null) return null;
        // Re-map: if filtering is active, we need to find the matching object.
        // We match by (checkerID, scheduleID) read from the model.
        int scheduleID = (int) tableModel.getValueAt(row, 1);
        String checkerName = (String) tableModel.getValueAt(row, 0);
        for (CheckerDetail cd : currentRows) {
            if (cd.getScheduleID() == scheduleID &&
                    (cd.getCheckerName() != null && cd.getCheckerName().equals(checkerName))) {
                return cd;
            }
        }
        return null;
    }

    // ── dialogs ───────────────────────────────────────────────────────────────

    private void showAddDialog() {
        CheckerDetailDialog dlg = new CheckerDetailDialog(
                SwingUtilities.getWindowAncestor(this), db, null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            CheckerDetail cd = dlg.getResult();
            boolean ok = db.createCheckerDetail(cd);
            if (ok) {
                // BUG 3 – auto-assign checker to matching class schedules
                int assigned = db.autoAssignCheckerToSchedules(cd);
                String msg = "Checker detail added successfully.";
                if (assigned > 0) {
                    msg += "\n" + assigned + " class schedule(s) automatically assigned to this checker.";
                }
                JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable(searchField.getText().trim());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to add checker detail. The checker/schedule combination may already exist.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditDialog() {
        CheckerDetail selected = selectedDetail();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a row to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        CheckerDetailDialog dlg = new CheckerDetailDialog(
                SwingUtilities.getWindowAncestor(this), db, selected);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            CheckerDetail updated = dlg.getResult();
            boolean ok = db.updateCheckerDetail(updated);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Checker detail updated successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable(searchField.getText().trim());
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update checker detail.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void confirmDelete() {
        CheckerDetail selected = selectedDetail();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String checkerLabel = selected.getCheckerName() != null
                ? selected.getCheckerName() : "Checker ID " + selected.getCheckerID();

        int confirm = JOptionPane.showConfirmDialog(this,
                "<html>Delete the following checker detail?<br><br>" +
                        "<b>Checker:</b> " + checkerLabel + "<br>" +
                        "<b>Schedule ID:</b> " + selected.getScheduleID() + "<br>" +
                        "<b>Day / Shift:</b> " + selected.getDay() + " | "
                        + selected.getShiftStart() + "–" + selected.getShiftEnd() + "<br><br>" +
                        "<i>Any class schedules assigned to this checker will have their<br>" +
                        "checker assignment cleared (set to none).</i></html>",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = db.deleteCheckerDetail(selected.getCheckerID(), selected.getScheduleID());
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Checker detail deleted. Affected class schedules have been unassigned.",
                        "Deleted", JOptionPane.INFORMATION_MESSAGE);
                refreshTable(searchField.getText().trim());
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete checker detail.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // Inner dialog for Add / Edit
    // =========================================================================

    static class CheckerDetailDialog extends JDialog {
        private boolean confirmed = false;
        private CheckerDetail result;
        private JComboBox<SystemUser> checkerCombo;
        private JComboBox<String> buildingCombo, floorCombo;
        private JCheckBox[] dayBoxes;
        private JSpinner startSpinner, endSpinner;
        private int autoScheduleID = -1;

        private static final String[] BUILDINGS = {"Main Building", "Science Building", "Engineering Complex", "Annex"};
        private static final String[] FLOORS = {"1st Floor", "2nd Floor", "3rd Floor", "4th Floor", "5th Floor"};
        private static final String[] DAYS = {
                "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
        };

        CheckerDetailDialog(Window owner, DataAccess db, CheckerDetail existing) {
            super(owner, existing == null ? "Add Checker Detail" : "Edit Checker Detail", ModalityType.APPLICATION_MODAL);
            setSize(500, 580);
            setLocationRelativeTo(owner);
            setResizable(false);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(UIHelper.SURFACE);
            panel.setBorder(new EmptyBorder(25, 30, 20, 30));

            // 1. Checker Selection (Name only)
            checkerCombo = new JComboBox<>();
            checkerCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object val, int idx, boolean sel, boolean foc) {
                    super.getListCellRendererComponent(list, val, idx, sel, foc);
                    if (val instanceof SystemUser) setText(((SystemUser) val).getName());
                    return this;
                }
            });
            db.getAllCheckerUsers().forEach(checkerCombo::addItem);
            panel.add(formRow("Checker", checkerCombo, 35));

            if (existing == null) autoScheduleID = db.getNextCheckerScheduleID();

            // 2. Day selection (Fixed spacing/clumping)
            JPanel dayPanel = new JPanel(new GridLayout(0, 3, 12, 10)); // 12px gap
            dayPanel.setOpaque(false);
            dayBoxes = new JCheckBox[DAYS.length];
            for (int i = 0; i < DAYS.length; i++) {
                dayBoxes[i] = new JCheckBox(DAYS[i]);
                dayBoxes[i].setOpaque(false);
                dayBoxes[i].setFont(UIHelper.FONT_TABLE);
                dayPanel.add(dayBoxes[i]);
            }
            panel.add(formRow("Day(s)", dayPanel, 100)); // Larger height for grid

            // 3. Time Spinners (30m increments)
            startSpinner = createTimeSpinner();
            endSpinner = createTimeSpinner();
            panel.add(formRow("Shift Start", startSpinner, 35));
            panel.add(formRow("Shift End", endSpinner, 35));

            // 4. Building & Floor Dropdowns
            buildingCombo = new JComboBox<>(BUILDINGS);
            floorCombo = new JComboBox<>(FLOORS);
            panel.add(formRow("Building", buildingCombo, 35));
            panel.add(formRow("Floor", floorCombo, 35));

            if (existing != null) prefill(existing);

            // 5. Action Buttons
            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            btns.setOpaque(false);
            JButton save = UIHelper.button("Save");
            JButton cancel = UIHelper.secondaryButton("Cancel");
            save.setPreferredSize(new Dimension(100, 35));
            cancel.setPreferredSize(new Dimension(100, 35));

            save.addActionListener(e -> onSave(existing));
            cancel.addActionListener(e -> dispose());

            btns.add(cancel);
            btns.add(save);

            panel.add(Box.createVerticalGlue());
            panel.add(btns);

            setContentPane(panel);
        }

        private JSpinner createTimeSpinner() {
            SpinnerDateModel model = new SpinnerDateModel() {
                @Override public Object getNextValue() {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(getDate());
                    cal.add(Calendar.MINUTE, 30);
                    return cal.getTime();
                }
                @Override public Object getPreviousValue() {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(getDate());
                    cal.add(Calendar.MINUTE, -30);
                    return cal.getTime();
                }
            };
            JSpinner spinner = new JSpinner(model);
            spinner.setEditor(new JSpinner.DateEditor(spinner, "HH:mm"));
            return spinner;
        }

        private void prefill(CheckerDetail existing) {
            for (int i = 0; i < checkerCombo.getItemCount(); i++) {
                if (checkerCombo.getItemAt(i).getUserID() == existing.getCheckerID()) {
                    checkerCombo.setSelectedIndex(i); break;
                }
            }
            checkerCombo.setEnabled(false);
            autoScheduleID = existing.getScheduleID();
            String days = existing.getDay() != null ? existing.getDay() : "";
            for (JCheckBox cb : dayBoxes) cb.setSelected(days.contains(cb.getText()));

            startSpinner.setValue(new java.util.Date(existing.getShiftStart().getTime()));
            endSpinner.setValue(new java.util.Date(existing.getShiftEnd().getTime()));
            buildingCombo.setSelectedItem(existing.getBuilding());
            floorCombo.setSelectedItem(existing.getFloor());
        }

        private void onSave(CheckerDetail existing) {
            // 1. Validate Days
            StringBuilder sb = new StringBuilder();
            for (JCheckBox cb : dayBoxes) {
                if (cb.isSelected()) sb.append(sb.length() > 0 ? "," : "").append(cb.getText());
            }
            if (sb.length() == 0) {
                JOptionPane.showMessageDialog(this, "Select at least one day", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. Validate Time Shift (Start must be before End)
            java.util.Date startTime = (java.util.Date) startSpinner.getValue();
            java.util.Date endTime = (java.util.Date) endSpinner.getValue();

            // Use Calendar to compare only hours and minutes (ignoring dates)
            Calendar calStart = Calendar.getInstance();
            calStart.setTime(startTime);
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(endTime);

            // Logic: Convert both to "minutes from midnight" for an easy comparison
            int startMins = calStart.get(Calendar.HOUR_OF_DAY) * 60 + calStart.get(Calendar.MINUTE);
            int endMins = calEnd.get(Calendar.HOUR_OF_DAY) * 60 + calEnd.get(Calendar.MINUTE);

            if (startMins >= endMins) {
                JOptionPane.showMessageDialog(this,
                        "Invalid Shift: Start time must be earlier than end time.",
                        "Time Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. Create Result if valid
            result = new CheckerDetail(
                    ((SystemUser) checkerCombo.getSelectedItem()).getUserID(),
                    autoScheduleID,
                    new java.sql.Time(startTime.getTime()),
                    new java.sql.Time(endTime.getTime()),
                    (String) buildingCombo.getSelectedItem(),
                    (String) floorCombo.getSelectedItem(),
                    sb.toString()
            );

            if (existing != null) result.setCheckerName(existing.getCheckerName());
            confirmed = true;
            dispose();
        }

        private JPanel formRow(String label, JComponent field, int height) {
            JPanel row = new JPanel();
            row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(450, height));

            JLabel lbl = new JLabel(label);
            lbl.setFont(UIHelper.FONT_LABEL);
            lbl.setPreferredSize(new Dimension(120, 30));

            // If it's a direct input (not a panel), constrain the width
            if (!(field instanceof JPanel)) {
                field.setMaximumSize(new Dimension(220, 30));
                field.setPreferredSize(new Dimension(220, 30));
            }

            row.add(lbl);
            row.add(Box.createHorizontalStrut(15));
            row.add(field);
            row.add(Box.createHorizontalGlue());

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setOpaque(false);
            wrapper.add(row, BorderLayout.CENTER);
            wrapper.setBorder(new EmptyBorder(0, 0, 15, 0));
            return wrapper;
        }

        boolean isConfirmed() { return confirmed; }
        CheckerDetail getResult() { return result; }
    }
}