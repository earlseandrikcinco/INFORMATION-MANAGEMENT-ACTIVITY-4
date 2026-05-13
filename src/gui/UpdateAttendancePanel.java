package gui;

import app.AppController;
import app.DataAccess;
import ref.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.util.List;

/**
 * Panel for recording or updating instructor attendance for a class session.
 * Checker: defaults to assigned classes, with a button to reveal all classes.
 * Admin / DeptHead / Secretary: always shows all classes.
 */
public class UpdateAttendancePanel extends BasePanel {

    private final DataAccess  db;
    private final SystemUser  currentUser;

    private DefaultTableModel   tableModel;
    private JTable              table;
    private List<ClassSchedule> displayedSchedules;
    private JButton             scopeBtn;
    private boolean             showingAll = false;

    // Status code ↔ label mapping (kept in one place so both arrays stay in sync)
    private static final String[] STATUS_LABELS = {
            "Present",
            "Absent (Unexcused)",
            "Sick Leave",
            "Official Business",
            "Personal Leave"
    };
    private static final String[] STATUS_CODES = {"P", "A", "SL", "OB", "PL"};

    // Statuses that require a leave request lookup
    private static final java.util.Set<String> LEAVE_STATUSES =
            java.util.Set.of("SL", "OB", "PL");

    public UpdateAttendancePanel(AppController controller, DataAccess db, SystemUser currentUser) {
        super(controller);
        this.db          = db;
        this.currentUser = currentUser;
        buildUI();
    }

    // ── Panel scaffold ────────────────────────────────────────────────────────

    private void buildUI() {
        String subtitle = isChecker()
                ? "My Assigned Classes  ·  " + currentUser.getName()
                : "All Classes  ·  " + currentUser.getRole();
        add(UIHelper.topBar("Update Attendance", subtitle), BorderLayout.NORTH);

        String[] cols = {"Class Code", "Course No.", "Days", "Start Time", "End Time", "Instructor"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = UIHelper.makeTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(70);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(180);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) markSelected();
            }
        });

        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setBackground(UIHelper.BG);
        body.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(UIHelper.sub("Double-click a class to record or update attendance for a date."), BorderLayout.WEST);

        if (isChecker()) {
            scopeBtn = UIHelper.secondaryButton("Show All Classes");
            scopeBtn.setPreferredSize(new Dimension(160, 30));
            scopeBtn.addActionListener(e -> toggleScope());
            topRow.add(scopeBtn, BorderLayout.EAST);
        }

        body.add(topRow, BorderLayout.NORTH);
        body.add(UIHelper.scroll(table), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        JButton markBtn = UIHelper.button("Mark Attendance →");
        markBtn.addActionListener(e -> markSelected());
        add(bottomBar(markBtn), BorderLayout.SOUTH);

        loadSchedules();
    }

    private void loadSchedules() {
        tableModel.setRowCount(0);
        displayedSchedules = (isChecker() && !showingAll)
                ? db.getSchedulesByCheckerID(currentUser.getUserID())
                : db.getAllClassSchedules();

        for (ClassSchedule cs : displayedSchedules) {
            tableModel.addRow(new Object[]{
                    cs.getClassCode(),
                    cs.getCourseNo(),
                    cs.getDays(),
                    cs.getStartTime(),
                    cs.getEndTime(),
                    cs.getInstructorName() != null ? cs.getInstructorName() : "—"
            });
        }
    }

    private void toggleScope() {
        showingAll = !showingAll;
        scopeBtn.setText(showingAll ? "Show My Classes Only" : "Show All Classes");
        loadSchedules();
    }

    private void markSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a class first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        showMarkDialog(displayedSchedules.get(row));
    }

    // ── Mark / edit dialog ────────────────────────────────────────────────────

    private void showMarkDialog(ClassSchedule cs) {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Record Attendance  —  " + cs.getClassCode(),
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(460, 400);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        // ── Header ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIHelper.ACCENT);
        header.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel titleLbl = new JLabel(cs.getCourseNo() + "  (" + cs.getClassCode() + ")");
        titleLbl.setFont(UIHelper.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);
        JLabel subLbl = new JLabel("Instructor: " + (cs.getInstructorName() != null ? cs.getInstructorName() : "N/A"));
        subLbl.setFont(UIHelper.FONT_SUB);
        subLbl.setForeground(new Color(200, 210, 255));
        header.add(titleLbl, BorderLayout.WEST);
        header.add(subLbl,   BorderLayout.SOUTH);

        // ── Form ────────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIHelper.BG);
        form.setBorder(BorderFactory.createEmptyBorder(16, 18, 8, 18));
        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(5, 0, 5, 10);
        lc.gridx  = 0;
        GridBagConstraints fc = new GridBagConstraints();
        fc.fill   = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets  = new Insets(5, 0, 5, 0);
        fc.gridx   = 1;

        // Date spinner
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner dateSpinner = new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));

        // Status combo
        JComboBox<String> statusCombo = new JComboBox<>(STATUS_LABELS);

        // Existing-record note
        JLabel existingNote = new JLabel(" ");
        existingNote.setFont(UIHelper.FONT_SUB.deriveFont(Font.ITALIC));

        // Remarks field  ← NEW
        JTextArea remarksArea = new JTextArea(3, 20);
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        remarksArea.setFont(UIHelper.FONT_SUB);
        JScrollPane remarksScroll = new JScrollPane(remarksArea);
        remarksScroll.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER));

        lc.gridy = 0; fc.gridy = 0;
        form.add(fieldLabel("Date:"),    lc);
        form.add(dateSpinner,            fc);

        lc.gridy = 1; fc.gridy = 1;
        form.add(fieldLabel("Status:"),  lc);
        form.add(statusCombo,            fc);

        lc.gridy = 2; fc.gridy = 2;
        form.add(new JLabel(),           lc);
        form.add(existingNote,           fc);

        lc.gridy = 3; fc.gridy = 3;
        lc.anchor = GridBagConstraints.NORTHWEST;
        form.add(fieldLabel("Remarks:"), lc);
        form.add(remarksScroll,          fc);
        lc.anchor = GridBagConstraints.WEST; // reset

        // ── Refresh note + pre-fill when date changes ────────────────────────
        Runnable refreshNote = () -> {
            java.util.Date d  = (java.util.Date) dateSpinner.getValue();
            Date sqlDate       = new Date(d.getTime());
            Attendance existing = db.getAttendanceForClass(cs.getClassCode(), sqlDate);

            if (existing != null) {
                existingNote.setText("⚠ Record exists (" + translateStatus(existing.getInstructorStatus()) + ") — will overwrite.");
                existingNote.setForeground(new Color(180, 100, 0));

                // Pre-fill status
                for (int i = 0; i < STATUS_CODES.length; i++) {
                    if (STATUS_CODES[i].equals(existing.getInstructorStatus())) {
                        statusCombo.setSelectedIndex(i);
                        break;
                    }
                }
                // Pre-fill remarks  ← NEW
                remarksArea.setText(existing.getRemarks() != null ? existing.getRemarks() : "");

            } else {
                existingNote.setText("No record yet — a new entry will be created.");
                existingNote.setForeground(UIHelper.TEXT_MID);
                remarksArea.setText("");
            }
        };

        dateSpinner.addChangeListener(e -> refreshNote.run());
        refreshNote.run();

        // ── Footer ───────────────────────────────────────────────────────────
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        foot.setBackground(UIHelper.BG);
        foot.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER));

        JButton cancelBtn = UIHelper.secondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = UIHelper.button("Save");
        saveBtn.setPreferredSize(new Dimension(90, 34));

        saveBtn.addActionListener(e -> {
            java.util.Date d = (java.util.Date) dateSpinner.getValue();
            Date sqlDate     = new Date(d.getTime());
            String code      = STATUS_CODES[statusCombo.getSelectedIndex()];
            String remarks   = remarksArea.getText().trim();   // ← from the new field

            // ── Resolve leaveRequestID ────────────────────────────────────
            //
            // Strategy (pure panel-side, no changes to upsertAttendance):
            //
            // 1. If an existing attendance record already exists for this
            //    class/date, reuse its leaveRequestID so we don't lose the link.
            //
            // 2. If the chosen status is a leave type (SL, OB, PL) and there
            //    is no existing record (or it had no leaveRequestID), look up
            //    an approved leave for the instructor that covers the chosen date.
            //    This requires one small DataAccess method — see note below.
            //
            // 3. For "Present" (P) or "Absent unexcused" (A), leaveRequestID
            //    is always null.

            Integer leaveRequestID = null;

            Attendance existing = db.getAttendanceForClass(cs.getClassCode(), sqlDate);

            if (existing != null && existing.getLeaveRequestID() != null) {
                // Case 1 — keep the existing link
                leaveRequestID = existing.getLeaveRequestID();

            } else if (LEAVE_STATUSES.contains(code) && cs.getInstructID() != null) {
                // Case 2 — look up an approved leave that covers this date
                // This calls the one new DataAccess method described below.
                LeaveRequest matchedLeave = db.getApprovedLeaveForInstructorOnDate(
                        cs.getInstructID(), sqlDate);
                if (matchedLeave != null) {
                    leaveRequestID = matchedLeave.getLeaveRequestID();
                }
                // If no approved leave found, leaveRequestID stays null.
                // That's valid — a checker may record a leave status before
                // the leave request is approved.
            }
            // Case 3 — P or A: leaveRequestID remains null (already set above)

            // ── Call upsert ───────────────────────────────────────────────
            boolean ok = db.upsertAttendance(
                    cs.getClassCode(),
                    sqlDate,
                    code,
                    currentUser.getUserID(),    // checkerID
                    cs.getInstructID(),         // actualInstructorID (assigned instructor)
                    leaveRequestID,
                    remarks.isEmpty() ? null : remarks
            );

            if (ok) {
                JOptionPane.showMessageDialog(dialog,
                        "Attendance recorded successfully.",
                        "Saved", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Failed to save attendance. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        foot.add(cancelBtn);
        foot.add(saveBtn);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(form,   BorderLayout.CENTER);
        dialog.add(foot,   BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isChecker() {
        return "Checker".equals(currentUser.getRole());
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIHelper.FONT_LABEL);
        l.setForeground(UIHelper.TEXT_MID);
        return l;
    }

    private String translateStatus(String code) {
        if (code == null) return "Unknown";
        return switch (code) {
            case "P"  -> "Present";
            case "A"  -> "Absent (Unexcused)";
            case "SL" -> "Sick Leave";
            case "OB" -> "Official Business";
            case "PL" -> "Personal Leave";
            default   -> code;
        };
    }
}