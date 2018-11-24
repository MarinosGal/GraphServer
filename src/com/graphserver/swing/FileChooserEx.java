package com.graphserver.swing;

import com.graphserver.data.FileUtil;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.io.File;
import static javax.swing.GroupLayout.DEFAULT_SIZE;

import javax.swing.*;

/**
 * Swing GUI for choosing a file
 * @author Marinos Galiatsatos
 */
public class FileChooserEx extends JFrame {

    private JPanel panel;
    private JTextArea area;

    public FileChooserEx() {
        initUI();
    }

    private void initUI() {

        panel = (JPanel) getContentPane();

        area = new JTextArea();

        JScrollPane spane = new JScrollPane();
        spane.getViewport().add(area);

        JToolBar toolbar = createToolBar();

        createLayout(toolbar, spane);

        setTitle("JFileChooser");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private JToolBar createToolBar() {

        ImageIcon open = new ImageIcon("document-open.png");

        JToolBar toolbar = new JToolBar();
        JButton openb = new JButton(open);

        openb.addActionListener(new OpenFileAction());

        return toolbar;
    }

    private void createLayout(JComponent... arg) {

        Container pane = getContentPane();
        GroupLayout gl = new GroupLayout(pane);
        pane.setLayout(gl);

        gl.setHorizontalGroup(gl.createParallelGroup()
                .addComponent(arg[0], DEFAULT_SIZE, DEFAULT_SIZE,
                        Short.MAX_VALUE)
                .addGroup(gl.createSequentialGroup()
                        .addComponent(arg[1]))
        );

        gl.setVerticalGroup(gl.createSequentialGroup()
                .addComponent(arg[0])
                .addGap(4)
                .addComponent(arg[1])
        );

        pack();
    }

    private class OpenFileAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            
            JFileChooser fdia = new JFileChooser();

            int ret = fdia.showDialog(panel, "Open file");

            if (ret == JFileChooser.APPROVE_OPTION) {
                File file = fdia.getSelectedFile();
                String text = FileUtil.readFile(file);
                area.setText(text);
            }
        }
    }

}
