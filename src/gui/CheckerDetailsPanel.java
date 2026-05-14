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
        // BUG 4: scheduleID is auto-incremented — no input field needed
        private int autoScheduleID = -1;
        // BUG 5: multiple-day checkboxes
        private JCheckBox[] dayBoxes;
        private JTextField shiftStartField;
        private JTextField shiftEndField;
        private JTextField buildingField;
        private JTextField floorField;

        /** Pass {@code existing = null} for "Add" mode, or an existing record for "Edit". */
        CheckerDetailDialog(Window owner, DataAccess db, CheckerDetail existing) {
            super(owner, existing == null ? "Add Checker Detail" : "Edit Checker Detail",
                    ModalityType.APPLICATION_MODAL);
            setSize(420, 420);
            setLocationRelativeTo(owner);
            setResizable(false);

            JPanel panel = new JPanel();
            panel.setBackground(UIHelper.SURFACE);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(new EmptyBorder(20, 28, 16, 28));

            // ── Checker combo ──────────────────────────────────────────────
            checkerCombo = new JComboBox<>();
            List<SystemUser> checkers = db.getAllCheckerUsers();
            for (SystemUser u : checkers) checkerCombo.addItem(u);
            panel.add(formRow("Checker", checkerCombo));
            panel.add(Box.createVerticalStrut(8));

            // BUG 4: Schedule ID is auto-incremented — no input field; auto-compute on save
            if (existing == null) {
                autoScheduleID = db.getNextCheckerScheduleID();
            }

            // BUG 5: Multi-day checkboxes
            JPanel dayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            dayPanel.setOpaque(false);
            dayBoxes = new JCheckBox[DAYS.length];
            for (int i = 0; i < DAYS.length; i++) {
                dayBoxes[i] = new JCheckBox(DAYS[i]);
                dayBoxes[i].setOpaque(false);
                dayBoxes[i].setFont(UIHelper.FONT_TABLE);
                dayPanel.add(dayBoxes[i]);
            }
            panel.add(formRow("Day(s)", dayPanel));
            panel.add(Box.createVerticalStrut(8));

            // ── Shift Start ────────────────────────────────────────────────
            shiftStartField = new JTextField();
            shiftStartField.setFont(UIHelper.FONT_TABLE);
            shiftStartField.setToolTipText("Format: HH:mm  (e.g. 07:00)");
            panel.add(formRow("Shift Start (HH:mm)", shiftStartField));
            panel.add(Box.createVerticalStrut(8));

            // ── Shift End ──────────────────────────────────────────────────
            shiftEndField = new JTextField();
            shiftEndField.setFont(UIHelper.FONT_TABLE);
            shiftEndField.setToolTipText("Format: HH:mm  (e.g. 12:00)");
            panel.add(formRow("Shift End (HH:mm)", shiftEndField));
            panel.add(Box.createVerticalStrut(8));

            // ── Building ───────────────────────────────────────────────────
            buildingField = new JTextField();
            buildingField.setFont(UIHelper.FONT_TABLE);
            panel.add(formRow("Building", buildingField));
            panel.add(Box.createVerticalStrut(8));

            // ── Floor ──────────────────────────────────────────────────────
            floorField = new JTextField();
            floorField.setFont(UIHelper.FONT_TABLE);
            panel.add(formRow("Floor", floorField));
            panel.add(Box.createVerticalStrut(16));

            // Pre-fill fields in edit mode
            if (existing != null) {
                for (int i = 0; i < checkerCombo.getItemCount(); i++) {
                    SystemUser u = checkerCombo.getItemAt(i);
                    if (u != null && u.getUserID() == existing.getCheckerID()) {
                        checkerCombo.setSelectedIndex(i);
                        break;
                    }
                }
                // Lock checker in edit mode — forms part of PK
                checkerCombo.setEnabled(false);
                autoScheduleID = existing.getScheduleID();
                // Pre-tick day checkboxes from comma-separated day string
                String existingDay = existing.getDay() != null ? existing.getDay() : "";
                for (JCheckBox cb : dayBoxes) {
                    cb.setSelected(existingDay.contains(cb.getText()));
                }
                shiftStartField.setText(existing.getShiftStart().toString());
                shiftEndField.setText(existing.getShiftEnd().toString());
                buildingField.setText(existing.getBuilding());
                floorField.setText(existing.getFloor());
            }

            // ── Buttons ────────────────────────────────────────────────────
            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            btns.setOpaque(false);
            JButton save   = UIHelper.button("Save");
            JButton cancel = UIHelper.secondaryButton("Cancel");
            save.setPreferredSize(new Dimension(90, 32));
            cancel.setPreferredSize(new Dimension(90, 32));
            save.addActionListener(e -> onSave(existing));
            cancel.addActionListener(e -> dispose());
            btns.add(cancel);
            btns.add(save);
            panel.add(btns);

            setContentPane(new JScrollPane(panel));
        }

        private void onSave(CheckerDetail existing) {
            // Validate
            SystemUser checker = (SystemUser) checkerCombo.getSelectedItem();
            if (checker == null) { warn("Please select a checker."); return; }
            // BUG 5: build day string from selected checkboxes
            StringBuilder dayBuilder = new StringBuilder();
            for (JCheckBox cb : dayBoxes) {
                if (cb.isSelected()) {
                    if (dayBuilder.length() > 0) dayBuilder.append(",");
                    dayBuilder.append(cb.getText());
                }
            }
            String day = dayBuilder.toString();
            if (day.isEmpty()) { warn("Please select at least one day."); return; }
            String start = shiftStartField.getText().trim();
            String end   = shiftEndField.getText().trim();
            if (!start.matches("\\d{2}:\\d{2}")) { warn("Shift Start must be HH:mm (e.g. 07:00)."); return; }
            if (!end.matches("\\d{2}:\\d{2}"))   { warn("Shift End must be HH:mm (e.g. 12:00)."); return; }
            String building = buildingField.getText().trim();
            String floor    = floorField.getText().trim();
            if (building.isEmpty()) { warn("Building is required."); return; }
            if (floor.isEmpty())    { warn("Floor is required."); return; }

            // BUG 4: use autoScheduleID (no user input)
            int sid = autoScheduleID;

            try {
                java.sql.Time startTime = java.sql.Time.valueOf(start + ":00");
                java.sql.Time endTime   = java.sql.Time.valueOf(end + ":00");

                result = new CheckerDetail(
                        checker.getUserID(),
                        sid,
                        startTime,
                        endTime,
                        building,
                        floor,
                        day
                );
            } catch (IllegalArgumentException e) {
                warn("Invalid time format. Use HH:mm.");
                return;
            }

            if (existing != null) result.setCheckerName(existing.getCheckerName());
            confirmed = true;
            dispose();
        }

        private void warn(String msg) {
            JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.WARNING_MESSAGE);
        }

        boolean isConfirmed() { return confirmed; }
        CheckerDetail getResult() { return result; }

        private static JPanel formRow(String label, JComponent field) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            JLabel lbl = new JLabel(label);
            lbl.setFont(UIHelper.FONT_LABEL);
            lbl.setForeground(UIHelper.TEXT_DARK);
            lbl.setPreferredSize(new Dimension(170, 28));
            row.add(lbl, BorderLayout.WEST);
            row.add(field, BorderLayout.CENTER);
            return row;
        }
    }
}