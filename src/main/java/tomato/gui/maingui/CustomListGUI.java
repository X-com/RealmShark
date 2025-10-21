package tomato.gui.maingui;

import tomato.backend.data.TomatoData;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public abstract class CustomListGUI extends JPanel {

    // Table columns: 0 = item name/id (String), 1 = remove button (String placeholder)
    private final JTable table;
    private final DefaultTableModel model;
    private final JDialog dialog;
    private final String propName;
    public String validationErrorMessage = "Invalid entry! Please correct the input.";

    public CustomListGUI(
            TomatoData data,
            String propName,
            String title,
            ArrayList<String> items,
            String tableHeader
    ) {
        CustomListGUI gui = this;
        this.propName = propName;

        setLayout(new BorderLayout());

        // Model with two columns: Item, Remove
        model = new DefaultTableModel(new Object[]{tableHeader, ""}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // allow editing the item text (col 0) and allow pressing the remove button (col 1)
                return column == 0 || column == 1;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return String.class;
                return Object.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.getColumnModel().getColumn(1).setMaxWidth(60);

        // Button renderer and editor for the remove column
        table.getColumnModel().getColumn(1).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(1).setCellEditor(new ButtonEditor());

        // Set a validating text editor for column 0 so invalid entries can't be committed
        table.getColumnModel().getColumn(0).setCellEditor(new ValidatingTextEditor());

        // Populate initial rows
        if (items != null && !items.isEmpty()) {
            for (String s : items) {
                model.addRow(new Object[]{s, "-"});
            }
        } else {
            model.addRow(new Object[]{"", "-"});
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with Add button
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("+");
        addButton.addActionListener(e -> {
            model.addRow(new Object[]{"", "-"});
            // scroll to bottom
            Rectangle rect = table.getCellRect(model.getRowCount() - 1, 0, true);
            table.scrollRectToVisible(rect);
        });
        bottom.add(addButton);
        add(bottom, BorderLayout.SOUTH);

        JOptionPane pane = getPane(data, gui);

        this.dialog = pane.createDialog(null, title);
    }

    private static JOptionPane getPane(TomatoData data, CustomListGUI gui) {
        JButton close = new JButton("Save");
        JOptionPane pane = new JOptionPane(gui, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, new JButton[]{close}, close);
        close.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(close);

            // If a cell is being edited, finish editing so the model reflects the latest text
            if (gui.table.isEditing()) {
                // Use the CellEditor interface which returns a boolean from stopCellEditing()
                javax.swing.CellEditor editor = gui.table.getCellEditor();
                if (editor != null) {
                    try {
                        boolean stopped = editor.stopCellEditing();
                        if (!stopped) {
                            // Editor refused to stop (validation failed) -> do not close/save
                            return;
                        }
                    } catch (Exception ignored) {
                        // If stopping the editor throws, abort save to be safe
                        return;
                    }
                }
            }

            pane.setValue(-1);
            w.dispose();

            // collect items from table model
            ArrayList<String> arr = new ArrayList<>();
            DefaultTableModel m = gui.model;
            for (int i = 0; i < m.getRowCount(); i++) {
                Object o = m.getValueAt(i, 0);
                if (o == null) continue;
                String v = o.toString().trim();
                if (!v.isEmpty() && !arr.contains(v)) {
                    arr.add(v);
                }
            }
            data.savePropList(arr, gui.propName);
        });
        return pane;
    }

    // Renderer for the remove button cell
    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    // Editor for the remove button cell
    private class ButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private final JButton button = new JButton();
        private Object currentValue;

        public ButtonEditor() {
            button.addActionListener(this);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.currentValue = value;
            button.setText(value == null ? "" : value.toString());
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return currentValue;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Determine the view row being edited, and map to model row
            final int viewRow = table.getEditingRow();
            final int modelRow = viewRow >= 0 ? table.convertRowIndexToModel(viewRow) : -1;

            // Stop editing first (this will call fireEditingStopped internally)
            // Then remove the row on the EDT after editing has fully stopped to avoid
            // ArrayIndexOutOfBoundsException from listeners that observe editing state.
            fireEditingStopped();

            if (modelRow >= 0) {
                SwingUtilities.invokeLater(() -> {
                    if (modelRow < model.getRowCount()) {
                        model.removeRow(modelRow);
                        // Ensure there's always at least one empty row for convenience
                        if (model.getRowCount() == 0) {
                            model.addRow(new Object[]{"", "-"});
                        }
                    }
                });
            }
        }
    }

    // Editor for column 0 that validates before allowing the edit to be committed
    private class ValidatingTextEditor extends DefaultCellEditor {
        public ValidatingTextEditor() {
            super(new JTextField());
        }

        @Override
        public boolean stopCellEditing() {
            Object value = getCellEditorValue();
            String text = value == null ? "" : value.toString();
            if (text != null && !CustomListGUI.this.validateEntry(text)) {
                // Validation failed; notify the user and keep the editor active
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(CustomListGUI.this),
                        validationErrorMessage, "Validation error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            return super.stopCellEditing();
        }
    }

    public void open() {
        this.dialog.setVisible(true);
    }

    protected abstract boolean validateEntry(String entry);
}
