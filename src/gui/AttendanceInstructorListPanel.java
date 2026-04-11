package gui;

import app.AppController;
import app.DataAccess;
import ref.Instructor;

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

    public AttendanceInstructorListPanel(AppController controller, DataAccess db) {
        super(controller);
        this.db = db;
        buildUI();
    }

    private void buildUI() {
        add(UIHelper.topBar("Attendance Records", "Select an instructor to view details"), BorderLayout.NORTH);

        instructors = db.getInstructorsWithLeaveRequests();

        String[] cols = {"ID", "Name", "Department"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for (Instructor i : instructors) {
            model.addRow(new Object[]{i.getInstructorID(), i.getName(), i.getDepartment()});
        }

        table = UIHelper.makeTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);

        // Double-click to drill in
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) viewSelected();
            }
        });

        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setBackground(UIHelper.BG);
        body.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));

        JLabel hint = UIHelper.sub(instructors.isEmpty()
                ? "No instructors with leave requests found."
                : "Instructors with leave requests: " + instructors.size() + "  —  double-click or select and press View Details");
        body.add(hint, BorderLayout.NORTH);
        body.add(UIHelper.scroll(table), BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);

        JButton viewBtn = UIHelper.button("View Details →");
        viewBtn.addActionListener(e -> viewSelected());
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