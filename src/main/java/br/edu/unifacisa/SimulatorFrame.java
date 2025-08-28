package br.edu.unifacisa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;

public class SimulatorFrame extends JFrame {
    private final JComboBox<String> algoCombo;
    private final MemoryManager manager;
    private final MemoryPanel memoryPanel;
    private final JTextField pidField;
    private final JSpinner sizeSpinner;
    private final JLabel statusLabel;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final Workload workload = new Workload();
    private final Map<String, Integer> processSize = new LinkedHashMap<>();

    public SimulatorFrame() {
        super("Simulador de Gerenciamento de Memória");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Algoritmo inicial
        AllocationAlgorithm algo = new FirstFit();
        manager = new MemoryManager(algo);
        memoryPanel = new MemoryPanel(manager);

        // Controles
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton runBtn = new JButton("Run");
        JButton stepBtn = new JButton("Step");
        JButton resetBtn = new JButton("Reset");
        JButton exitBtn = new JButton("Exit");
        top.add(runBtn);
        top.add(stepBtn);
        top.add(resetBtn);
        top.add(exitBtn);

        // Painel direito com informações
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        algoCombo = new JComboBox<>(new String[]{"First Fit", "Next Fit", "Best Fit"});
        right.add(new JLabel("Algoritmo:"));
        right.add(algoCombo);
        right.add(Box.createVerticalStrut(6));

        JPanel allocPanel = new JPanel(new GridLayout(0, 2, 6, 6));
        pidField = new JTextField("P" + (int) (Math.random() * 100));
        sizeSpinner = new JSpinner(new SpinnerNumberModel(8, 2, 128, 2));
        allocPanel.add(new JLabel("Processo (ID):"));
        allocPanel.add(pidField);
        allocPanel.add(new JLabel("Tamanho (KB):"));
        allocPanel.add(sizeSpinner);
        JButton allocBtn = new JButton("Alocar");
        JButton freeBtn = new JButton("Liberar (ID)");
        allocPanel.add(allocBtn);
        allocPanel.add(freeBtn);
        right.add(allocPanel);

        JButton demoBtn = new JButton("Gerar Carga");
        right.add(Box.createVerticalStrut(6));
        right.add(demoBtn);

        right.add(Box.createVerticalStrut(6));
        statusLabel = new JLabel(statusText("STOP"));
        right.add(statusLabel);

        // Tabela de processos em execução
        tableModel = new DefaultTableModel(new Object[]{"ID", "Alocado (KB)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(240, 220));

        add(top, BorderLayout.NORTH);
        add(memoryPanel, BorderLayout.CENTER);
        JPanel east = new JPanel(new BorderLayout());
        east.add(right, BorderLayout.NORTH);
        east.add(tableScroll, BorderLayout.CENTER);
        add(east, BorderLayout.EAST);

        // Escolha de algoritmos
        algoCombo.addActionListener(e -> {
            String sel = (String) algoCombo.getSelectedItem();
            if ("First Fit".equals(sel)) manager.setAlgorithm(new FirstFit());
            else if ("Next Fit".equals(sel)) manager.setAlgorithm(new NextFit());
            else manager.setAlgorithm(new BestFit());
        });

        allocBtn.addActionListener(e -> onAlloc());
        freeBtn.addActionListener(e -> onFree());
        demoBtn.addActionListener(e -> onGenerateDemo());
        stepBtn.addActionListener(e -> onStep());
        runBtn.addActionListener(e -> onRun());
        resetBtn.addActionListener(e -> onReset());
        exitBtn.addActionListener((ActionEvent e) -> System.exit(0));

        pack();
        setLocationRelativeTo(null);
    }

    private String statusText(String state) {
        return "status: " + state + "    time: " + manager.getClock();
    }

    private void refresh() {
        memoryPanel.repaint();
        // Atualizar tabela de processos
        tableModel.setRowCount(0);
        for (Map.Entry<String, Integer> en : processSize.entrySet()) {
            tableModel.addRow(new Object[]{en.getKey(), en.getValue()});
        }
        statusLabel.setText(statusText("RUNNING"));
    }

    private void onAlloc() {
        String pid = pidField.getText().trim();
        int size = (Integer) sizeSpinner.getValue();
        if (pid.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe um ID.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean ok = manager.allocate(pid, size);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Falha na alocação. Memória insuficiente", "Erro", JOptionPane.ERROR_MESSAGE);
        } else {
            processSize.put(pid, processSize.getOrDefault(pid, 0) + MemoryManager.align(size));
        }
        refresh();
    }

    private void onFree() {
        String pid = pidField.getText().trim();
        if (pid.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe um ID para liberar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int released = manager.free(pid);
        if (released == 0) {
            JOptionPane.showMessageDialog(this, "ID não encontrado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
        } else {
            processSize.remove(pid);
        }
        refresh();
    }

    private void onGenerateDemo() {
        workload.clear();
        Workload demo = Workload.demo();

        Operation op;
        while ((op = demo.poll()) != null) workload.add(op);
        JOptionPane.showMessageDialog(this, "Carga de demonstração criada. Use Step/Run.", "OK", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onStep() {
        stepOnce(true);
    }

    private void stepOnce(boolean showDialogs) {
        Operation op = workload.poll();
        if (op == null) {
            if (showDialogs) {
                JOptionPane.showMessageDialog(this, "Sem operações pendentes.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
            return;
        }
        if (op.type == Operation.Type.ALLOC) {
            boolean ok = manager.allocate(op.pid, op.sizeKb);
            if (ok) processSize.put(op.pid, processSize.getOrDefault(op.pid, 0) + MemoryManager.align(op.sizeKb));
        } else {
            manager.free(op.pid);
            processSize.remove(op.pid);
        }
        refresh();
    }


    private void onRun() {
        if (workload.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Gere a carga primeiro (Gerar Carga).", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // roda o laço fora da EDT
        new Thread(() -> {
            while (!workload.isEmpty()) {
                try {
                    // aplica um passo na EDT para manter a UI segura
                    javax.swing.SwingUtilities.invokeAndWait(() -> stepOnce(false));
                    Thread.sleep(120);
                } catch (Exception ignored) {
                }
            }
            javax.swing.SwingUtilities.invokeLater(() -> statusLabel.setText(statusText("STOP")));
        }).start();
    }


    private void onReset() {
        manager.reset();
        processSize.clear();
        workload.clear();
        statusLabel.setText(statusText("STOP"));
        refresh();
    }
}