package gui;

import app.AppController;
import app.DataAccess;
import ref.DeptHead;
import ref.Instructor;
import ref.Secretary;
import ref.SystemUser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class AttendanceInstructorListPanel extends BasePanel {

    private final DataAccess db;
    private JTable table;
    private List<Instructor> instructors;
    private final SystemUser currentUser;
    private final int deptID;

    public AttendanceInstructorListPanel(AppController controller, DataAccess db, SystemUser user) {
        super(controller);
        this.db = db;
        this.currentUser = user;
        if (currentUser instanceof Secretary secretary) {
            deptID = (secretary).getDepartmentID();
        } else if (currentUser instanceof DeptHead deptHead) {
            deptID = (deptHead).getDepartmentID();
        } else {
            deptID = -1;
        }
        buildUI();
    }

    private void buildUI() {
        add(UIHelper.topBar("Attendance Records", "Select an instructor to view details"), BorderLayout.NORTH);

        if (deptID == -1) {
            instructors = db.getInstructors();
        } else {
            instructors = db.getInstructorsByDept(deptID);
        }

        String[] cols;
        if (deptID == -1) {
            cols = new String[]{"ID", "Name", "Department"};
        } else {
            cols = new String[]{"ID", "Name"};
        }

        DefaultTableModel model = new DefaultTableModel(cols, 0);

        for (Instructor i : instructors) {
            if (deptID == -1) {
                model.addRow(new Object[]{
                        i.getInstructorID(),
                        i.getName(),
                        i.getDepartmentName()
                });
            } else {
                model.addRow(new Object[]{
                        i.getInstructorID(),
                        i.getName()
                });
            }
        }

        table = UIHelper.makeTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        if (deptID == -1) {
            table.getColumnModel().getColumn(2).setPreferredWidth(180);
        }

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) viewSelected();
            }
        });

        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setBackground(UIHelper.BG);
        body.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));

        String hintText = instructors.isEmpty()
                ? "No instructors found."
                : "Instructors found: " + instructors.size() + "  —  double-click to view details";
        JLabel hint = UIHelper.sub(hintText);

        body.add(hint, BorderLayout.NORTH);
        body.add(UIHelper.scroll(table), BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);

        JButton viewBtn = UIHelper.button("View Details →");
        viewBtn.addActionListener(_ -> viewSelected());
        add(bottomBar(viewBtn), BorderLayout.SOUTH);
    }

    private void viewSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an instructor first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        controller.showAttendanceDetail(instructors.get(row));
    }
}