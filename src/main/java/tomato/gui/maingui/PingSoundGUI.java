package tomato.gui.maingui;

import java.awt.*;
import java.io.File;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import tomato.realmshark.PingSounds;

/**
 * Per-category ping sound picker.
 *
 * One row per ping type: a dropdown of the bundled clips plus the Windows
 * notification library, a Browse button for anything else, and a Test button
 * so a sound can be heard before committing to it.
 */
public class PingSoundGUI extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Map<PingSounds.Type, JComboBox<String>> pickers =
        new EnumMap<>(PingSounds.Type.class);

    /** Full paths backing the dropdown entries, parallel to their indexes. */
    private final Map<PingSounds.Type, DefaultComboBoxModel<String>> models =
        new EnumMap<>(PingSounds.Type.class);

    private final Map<PingSounds.Type, String> chosen = new EnumMap<>(
        PingSounds.Type.class
    );

    public PingSoundGUI() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        List<String> available = PingSounds.availableSounds();

        JPanel rows = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        int row = 0;
        for (PingSounds.Type type : PingSounds.Type.values()) {
            String current = PingSounds.getPath(type);
            chosen.put(type, current);

            c.gridx = 0;
            c.gridy = row;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            rows.add(new JLabel(type.label + ":"), c);

            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            for (String path : available) {
                model.addElement(path);
            }
            // A previously browsed-to file may sit outside both folders.
            if (model.getIndexOf(current) < 0) {
                model.addElement(current);
            }
            model.setSelectedItem(current);

            JComboBox<String> combo = new JComboBox<>(model);
            // Show file names, keep full paths as the values.
            combo.setRenderer(
                new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(
                        JList<?> list,
                        Object value,
                        int index,
                        boolean isSelected,
                        boolean cellHasFocus
                    ) {
                        super.getListCellRendererComponent(
                            list,
                            value,
                            index,
                            isSelected,
                            cellHasFocus
                        );
                        setText(PingSounds.displayName((String) value));
                        return this;
                    }
                }
            );
            combo.setPreferredSize(new Dimension(240, 26));
            combo.addActionListener(e -> {
                Object sel = combo.getSelectedItem();
                if (sel != null) chosen.put(type, (String) sel);
            });

            c.gridx = 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            rows.add(combo, c);

            JButton browse = new JButton("Browse…");
            browse.addActionListener(e -> browseFor(type, model, combo));
            c.gridx = 2;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            rows.add(browse, c);

            JButton test = new JButton("Test");
            test.addActionListener(e -> {
                // Apply first so the test plays what is actually selected.
                PingSounds.setPath(type, chosen.get(type));
                PingSounds.play(type);
            });
            c.gridx = 3;
            rows.add(test, c);

            pickers.put(type, combo);
            models.put(type, model);
            row++;
        }

        add(rows, BorderLayout.CENTER);

        JLabel hint = new JLabel(
            "<html><i>Sounds are taken from Tomato's sound folder and the " +
            "Windows notification library (C:\\Windows\\Media).<br>" +
            "Use Browse to pick any other .wav file.</i></html>"
        );
        hint.setBorder(BorderFactory.createEmptyBorder(8, 4, 0, 4));
        add(hint, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton defaults = new JButton("Restore Defaults");
        JButton save = new JButton("Save");
        bottom.add(defaults);
        bottom.add(save);
        add(bottom, BorderLayout.SOUTH);

        defaults.addActionListener(e -> {
            for (PingSounds.Type type : PingSounds.Type.values()) {
                PingSounds.setPath(type, null);
                String reset = PingSounds.getPath(type);
                chosen.put(type, reset);
                DefaultComboBoxModel<String> m = models.get(type);
                if (m.getIndexOf(reset) < 0) m.addElement(reset);
                m.setSelectedItem(reset);
            }
        });

        save.addActionListener(e -> {
            for (PingSounds.Type type : PingSounds.Type.values()) {
                PingSounds.setPath(type, chosen.get(type));
            }
            JOptionPane.showMessageDialog(
                PingSoundGUI.this,
                "Ping sounds saved.",
                "Save",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    private void browseFor(
        PingSounds.Type type,
        DefaultComboBoxModel<String> model,
        JComboBox<String> combo
    ) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select a .wav for " + type.label);
        fc.setFileFilter(new FileNameExtensionFilter("WAV audio", "wav"));
        String current = chosen.get(type);
        if (current != null) {
            File f = new File(current);
            if (f.getParentFile() != null && f.getParentFile().isDirectory()) {
                fc.setCurrentDirectory(f.getParentFile());
            }
        }
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getAbsolutePath();
            if (model.getIndexOf(path) < 0) model.addElement(path);
            model.setSelectedItem(path);
            combo.setSelectedItem(path);
            chosen.put(type, path);
        }
    }

    /**
     * Opens the ping sound configuration dialog.
     */
    public static void open() {
        JFrame parent = tomato.gui.TomatoGUI.getFrame();
        PingSoundGUI panel = new PingSoundGUI();
        JDialog dialog = new JDialog(parent, "Ping Sounds", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}
