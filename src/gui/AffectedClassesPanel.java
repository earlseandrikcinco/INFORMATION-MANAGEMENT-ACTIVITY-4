package gui;

import app.AppController;
import app.DataAccess;
import ref.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AffectedClassesPanel extends BasePanel {
    private final DataAccess db;
    private final LeaveRequest leaveRequest;
    private final SystemUser currentUser;
    private DefaultTableModel tableModel;
    private JTable table;
    private List<AffectedClass> affectedList;

    public AffectedClassesPanel(
            AppController controller,
            DataAccess db,
            LeaveRequest leaveRequest,
            SystemUser currentUser) {
        super(controller);
        this.db           = db;
        this.leaveRequest = leaveRequest;
        this.currentUser  = currentUser;
        buildUI();
    }

    private void buildUI() {
        // Top bar: leave summary
        add(UIHelper.topBar(
                "Affected Classes",
                leaveRequest.getInstructorName() + "  ·  " +
                        leaveRequest.getLeaveType()      + "  ·  " +
                        leaveRequest.getStartDate()      + " → " +
                        leaveRequest.getEndDate()
        ), BorderLayout.NORTH);

        // Table
        String[] cols = {"Date", "Course", "Time", "Building / Floor", "Resolution"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = UIHelper.makeTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);

        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setBackground(UIHelper.BG);
        body.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));
        body.add(UIHelper.scroll(table), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        // Action buttons
        JButton substituteBtn = UIHelper.button("Assign Substitute");
        JButton asyncBtn      = UIHelper.secondaryButton("Make Asynchronous");
        JButton backBtn       = UIHelper.secondaryButton("← Back");

        substituteBtn.addActionListener(e -> showSubstituteDialog());
        asyncBtn.addActionListener(e -> makeSelectedAsync());
        backBtn.addActionListener(e -> controller.showLeaveRequests());

        add(bottomBar(substituteBtn, asyncBtn, backBtn), BorderLayout.SOUTH);

        loadAffectedClasses();
    }

    private void loadAffectedClasses() {
        affectedList = db.getAffectedClassesByLeave(leaveRequest.getLeaveRequestID());
        tableModel.setRowCount(0);
        for (AffectedClass ac : affectedList) {
            String resolution;
            switch (ac.getInstructorStatus()) {
                case "Substituted"  -> resolution = "Sub: " + ac.getSubstituteName();
                case "Asynchronous" -> resolution = "Asynchronous";
                default            -> resolution = "Unresolved";
            }
            tableModel.addRow(new Object[]{
                    ac.getDate(),
                    ac.getCourseNo(),
                    ac.getStartTime() + " – " + ac.getEndTime(),
                    ac.getBuilding()  + " / " + ac.getFloor(),
                    resolution
            });
        }
    }

    private void showSubstituteDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a class session first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AffectedClass ac = affectedList.get(row);

        List<Instructor> subs = db.getAvailableSubstitutes(leaveRequest.getInstructID());
        if (subs.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No available substitutes found.",
                    "No Substitutes", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] names = subs.stream()
                .map(Instructor::getName)
                .toArray(String[]::new);

        String chosen = (String) JOptionPane.showInputDialog(
                this,
                "Select substitute instructor for:\n" +
                        ac.getCourseNo() + "  on  " + ac.getDate(),
                "Assign Substitute",
                JOptionPane.PLAIN_MESSAGE,
                null, names, names[0]
        );
        if (chosen == null) return;

        Instructor sub = subs.stream()
                .filter(i -> i.getName().equals(chosen))
                .findFirst().orElse(null);
        if (sub == null) return;

        boolean ok = db.assignSubstitute(ac.getAttendanceID(), sub.getInstructorID());
        if (ok) {
            loadAffectedClasses();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to assign substitute. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void makeSelectedAsync() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a class session first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AffectedClass ac = affectedList.get(row);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Mark this session as Asynchronous?\n" +
                        ac.getCourseNo() + "  on  " + ac.getDate(),
                "Make Asynchronous", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = db.makeClassAsynchronous(ac.getAttendanceID());
        if (ok) {
            loadAffectedClasses();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to update. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
