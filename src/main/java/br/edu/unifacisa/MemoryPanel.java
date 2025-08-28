package br.edu.unifacisa;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Renderiza a memória como uma grade 8x8 de unidades de 2 KB (total de 128 KB)
 */
public class MemoryPanel extends JPanel {
    private final MemoryManager manager;
    private final Color[] palette;

    public MemoryPanel(MemoryManager manager) {
        this.manager = manager;
        setPreferredSize(new Dimension(360, 360));
        // paleta de cores dos processos
        palette = new Color[]{
                new Color(0x6F8FAF), // azul
                new Color(0x5D6D7E), // cinza aço
                new Color(0x1ABC9C), // verde água frio
                new Color(0x2E86C1), // azul médio
                new Color(0x117A65), // verde escuro
                new Color(0x1B4F72), // azul escuro
                new Color(0x145A32),  // verde bem escuro
                new Color(0x2C3E50), // azul petróleo
                new Color(0x34495E), // cinza azulado
                new Color(0x21618C) // azul profundo

        };
    }

    // Sobrescreve o metodo de pintura do painel: desenha a memória como uma grade 8x8
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int cols = 8, rows = 8;
        int w = getWidth();
        int h = getHeight();
        int cellW = w / cols;
        int cellH = h / rows;

        List<String> pidOrder = new ArrayList<>();
        int[] owners = manager.snapshotUnitOwners(pidOrder);

        int idx = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = c * cellW;
                int y = r * cellH;
                int owner = owners[idx++];
                if (owner >= 0) {
                    Color color = palette[owner % palette.length];
                    g2.setColor(color);
                    g2.fillRect(x + 1, y + 1, cellW - 2, cellH - 2);
                } else {
                    g2.setColor(new Color(240, 240, 240));
                    g2.fillRect(x + 1, y + 1, cellW - 2, cellH - 2);

                    // desenha linha diagonal riscando espaço livre quando vazio
                    g2.setColor(new Color(200, 200, 200));
                    g2.drawLine(x + 2, y + 2, x + cellW - 3, y + cellH - 3);
                }
                g2.setColor(new Color(180, 180, 180));
                g2.drawRect(x, y, cellW, cellH);
            }
        }

        // legenda com o espaço total, livre e usado da memória
        int legendY = 14;
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("128KB total • 2KB/unidade • Livre: " + manager.freeKb() + "KB • Uso: " + manager.usedKb() + "KB", 8, legendY);
    }
}