package gui;

import app.DataAccess;
import ref.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Modal dialog showing full details of one ClassSchedule, resolved through FK lookups.
 *
 * Role-based edit rights:
 *   - Admin      → can assign/change checker only
 *   - DeptHead   → can assign/change instructor AND checker
 *   - Secretary  → can assign/change instructor AND checker
 */
public class ClassScheduleDetailDialog extends JDialog {

    private final DataAccess  db;
    private final SystemUser  currentUser;
    private final ClassSchedule schedule;
    private final Runnable    onSaved;   // called after a successful save so parent can refresh

    // Edit controls (null when the role cannot edit that field)
    private JComboBox<Instructor> instructorCombo;  // DeptHead / Secretary only
    private JComboBox<SystemUser> checkerCombo;     // all three roles

    public ClassScheduleDetailDialog(Frame parent, DataAccess db,
                                     SystemUser currentUser,
                                     ClassSchedule schedule,
                                     Runnable onSaved) {
        super(parent, "Class Schedule Details", true);
        this.db          = db;
        this.currentUser = currentUser;
        this.schedule    = schedule;
        this.onSaved     = onSaved;

        buildUI();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void buildUI() {
        String role = currentUser.getRole();
        boolean canEditInstructor = role.equals("DeptHead") || role.equals("Secretary");
        boolean canEditChecker    = role.equals("Admin") || canEditInstructor;

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(18, 24, 12, 24));
        root.setBackground(UIHelper.SURFACE);

        // ── Header ──────────────────────────────────────────────────────────
        JLabel header = UIHelper.title("Schedule: " + schedule.getClassCode());
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(header);
        root.add(Box.createVerticalStrut(14));

        // ── Read-only info ───────────────────────────────────────────────────
        JPanel info = new JPanel(new GridLayout(0, 2, 8, 6));
        info.setBackground(UIHelper.SURFACE);
        info.setAlignmentX(Component.LEFT_ALIGNMENT);

        addReadRow(info, "Class Code:",  String.valueOf(schedule.getClassCode()));
        addReadRow(info, "Course No.:",  schedule.getCourseNo());
        addReadRow(info, "Days:",        schedule.getDays());
        addReadRow(info, "Start Time:",  String.valueOf(schedule.getStartTime()));
        addReadRow(info, "End Time:",    String.valueOf(schedule.getEndTime()));

        // Room — resolved via roomDescription set by DataAccess.getScheduleWithDetails
        String roomDisplay = schedule.getRoomDescription() != null
                ? schedule.getRoomDescription()
                : (schedule.getRoomID() != null ? "Room ID " + schedule.getRoomID() : "—");
        addReadRow(info, "Room:", roomDisplay);

        root.add(info);
        root.add(Box.createVerticalStrut(16));

        // ── Instructor (editable for DeptHead / Secretary) ───────────────────
        JPanel instructorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        instructorRow.setBackground(UIHelper.SURFACE);
        instructorRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel instrLabel = UIHelper.sub("Instructor: ");
        instrLabel.setPreferredSize(new Dimension(120, 24));
        instructorRow.add(instrLabel);

        if (canEditInstructor) {
            // Build list of instructors for this dept (or all for admin, but admin can't edit instructor)
            List<Instructor> instructors = getInstructorList();
            instructorCombo = new JComboBox<>(instructors.toArray(new Instructor[0]));
            instructorCombo.insertItemAt(null, 0);   // allow "unassigned"
            instructorCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value,
                                                              int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value == null) setText("— Unassigned —");
                    return this;
                }
            });

            // Pre-select current instructor
            if (schedule.getInstructID() != null) {
                for (int i = 1; i < instructorCombo.getItemCount(); i++) {
                    Instructor inst = instructorCombo.getItemAt(i);
                    if (inst != null && inst.getInstructorID() == schedule.getInstructID()) {
                        instructorCombo.setSelectedIndex(i);
                        break;
                    }
                }
            } else {
                instructorCombo.setSelectedIndex(0);
            }

            instructorCombo.setPreferredSize(new Dimension(230, 26));
            instructorRow.add(instructorCombo);
        } else {
            // Read-only
            String instrName = schedule.getInstructorName() != null
                    ? schedule.getInstructorName() : "—";
            instructorRow.add(UIHelper.sub(instrName));
        }

        root.add(instructorRow);
        root.add(Box.createVerticalStrut(10));

        // ── Checker (editable for Admin, DeptHead, Secretary) ────────────────
        JPanel checkerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        checkerRow.setBackground(UIHelper.SURFACE);
        checkerRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel checkerLabel = UIHelper.sub("Assigned Checker: ");
        checkerLabel.setPreferredSize(new Dimension(120, 24));
        checkerRow.add(checkerLabel);

        if (canEditChecker) {
            List<SystemUser> checkers = db.getCheckers();
            checkerCombo = new JComboBox<>(checkers.toArray(new SystemUser[0]));
            checkerCombo.insertItemAt(null, 0);
            checkerCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value,
                                                              int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value == null) setText("— Unassigned —");
                    else setText(((SystemUser) value).getName());
                    return this;
                }
            });

            // Pre-select current checker by userID == assignedChecker
            if (schedule.getAssignedChecker() != null) {
                for (int i = 1; i < checkerCombo.getItemCount(); i++) {
                    SystemUser c = checkerCombo.getItemAt(i);
                    if (c != null && c.getUserID() == schedule.getAssignedChecker()) {
                        checkerCombo.setSelectedIndex(i);
                        break;
                    }
                }
            } else {
                checkerCombo.setSelectedIndex(0);
            }

            checkerCombo.setPreferredSize(new Dimension(230, 26));
            checkerRow.add(checkerCombo);
        } else {
            String checkerName = schedule.getCheckerName() != null
                    ? schedule.getCheckerName() : "—";
            checkerRow.add(UIHelper.sub(checkerName));
        }

        root.add(checkerRow);
        root.add(Box.createVerticalStrut(20));

        // ── Buttons ──────────────────────────────────────────────────────────
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(UIHelper.SURFACE);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton cancelBtn = UIHelper.secondaryButton("Close");
        cancelBtn.addActionListener(e -> dispose());
        btnRow.add(cancelBtn);

        if (canEditChecker) {   // at least one field is editable
            JButton saveBtn = UIHelper.button("Save Changes");
            saveBtn.addActionListener(e -> saveChanges(canEditInstructor));
            btnRow.add(saveBtn);
        }

        root.add(btnRow);

        setContentPane(root);
    }

    private void saveChanges(boolean canEditInstructor) {
        Integer newInstructID = schedule.getInstructID();
        if (canEditInstructor && instructorCombo != null) {
            Instructor sel = (Instructor) instructorCombo.getSelectedItem();
            newInstructID = (sel == null) ? null : sel.getInstructorID();
        }

        Integer newCheckerID = schedule.getAssignedChecker();
        if (checkerCombo != null) {
            SystemUser sel = (SystemUser) checkerCombo.getSelectedItem();
            newCheckerID = (sel == null) ? null : sel.getUserID();
        }

        boolean ok = db.updateClassScheduleAssignments(schedule.getClassCode(), newInstructID, newCheckerID);

        if (ok) {
            JOptionPane.showMessageDialog(this, "Schedule updated successfully.",
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
            if (onSaved != null) onSaved.run();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save changes. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Returns instructors scoped to the user's department (DeptHead/Secretary),
     * or all instructors if admin somehow reaches here.
     */
    private List<Instructor> getInstructorList() {
        String role = currentUser.getRole();
        if (role.equals("DeptHead")) {
            return db.getInstructorsByDept(((DeptHead) currentUser).getDepartmentID());
        } else if (role.equals("Secretary")) {
            return db.getInstructorsByDept(((Secretary) currentUser).getDepartmentID());
        }
        return db.getInstructors();
    }

    // ── Helper ───────────────────────────────────────────────────────────────
    private void addReadRow(JPanel panel, String label, String value) {
        JLabel lbl = UIHelper.sub(label);
        lbl.setFont(UIHelper.FONT_LABEL);
        panel.add(lbl);
        panel.add(UIHelper.sub(value != null ? value : "—"));
    }
}