package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class UIHelper {

    public static final Color BG         = new Color(245, 245, 247);
    public static final Color SURFACE    = Color.WHITE;
    public static final Color ACCENT     = new Color(55, 85, 170);
    public static final Color TEXT_DARK  = new Color(25, 25, 25);
    public static final Color TEXT_MID   = new Color(90, 90, 100);
    public static final Color BORDER     = new Color(210, 210, 215);
    public static final Color ROW_ALT    = new Color(237, 241, 255);

    public static final Font FONT_TITLE  = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_SUB    = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_LABEL  = new Font("SansSerif", Font.BOLD, 12);
    public static final Font FONT_TABLE  = new Font("SansSerif", Font.PLAIN, 12);

    private UIHelper() {}

    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE);
        l.setForeground(TEXT_DARK);
        return l;
    }

    public static JLabel sub(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SUB);
        l.setForeground(TEXT_MID);
        return l;
    }

    public static JButton button(String label) {
        JButton b = new JButton(label);
        b.setFont(FONT_LABEL);
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(170, 34));
        return b;
    }

    public static JButton secondaryButton(String label) {
        JButton b = new JButton(label);
        b.setFont(FONT_LABEL);
        b.setBackground(new Color(195, 195, 200));
        b.setForeground(TEXT_DARK);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(100, 30));
        return b;
    }

    public static JTable makeTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? SURFACE : ROW_ALT);
                return c;
            }
        };
        table.setFont(FONT_TABLE);
        table.getTableHeader().setFont(FONT_LABEL);
        table.getTableHeader().setBackground(ACCENT);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setRowHeight(24);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(180, 200, 255));
        table.setSelectionForeground(TEXT_DARK);
        return table;
    }

    public static JScrollPane scroll(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(SURFACE);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        return sp;
    }

    public static JPanel topBar(String title, String subtitle) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(ACCENT);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        JLabel t = new JLabel(title);
        t.setFont(FONT_TITLE);
        t.setForeground(Color.WHITE);
        bar.add(t, BorderLayout.WEST);
        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel s = new JLabel(subtitle);
            s.setFont(FONT_SUB);
            s.setForeground(new Color(200, 210, 255));
            bar.add(s, BorderLayout.EAST);
        }
        return bar;
    }

    public static JPanel statChip(String label, String value) {
        JPanel p = new JPanel();
        p.setBackground(SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_SUB);
        lbl.setForeground(TEXT_MID);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.BOLD, 28));
        val.setForeground(ACCENT);
        val.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(lbl);
        p.add(Box.createVerticalStrut(4));
        p.add(val);
        return p;
    }
}