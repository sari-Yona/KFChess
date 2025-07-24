package org.kamatech.chess.listeners;

import org.kamatech.chess.events.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Listener for creating beautiful game animations
 */
public class AnimationListener implements EventListener<org.kamatech.chess.events.Event> {
    private JFrame gameFrame;
    private JPanel gamePanel;
    
    public AnimationListener(JFrame gameFrame, JPanel gamePanel) {
        this.gameFrame = gameFrame;
        this.gamePanel = gamePanel;
    }
    
    @Override
    public void onEvent(org.kamatech.chess.events.Event event) {
        if (event instanceof GameStartedEvent) {
            showGameStartAnimation();
        } else if (event instanceof GameEndedEvent) {
            showGameEndAnimation();
        }
    }
    
    /**
     * Show beautiful game start animation
     */
    private void showGameStartAnimation() {
        SwingUtilities.invokeLater(() -> {
            // Create animation overlay
            final JPanel[] animationPanelHolder = new JPanel[1];
            
            JPanel animationPanel = new JPanel() {
                private int frame = 0;
                private Timer animationTimer;
                
                {
                    setOpaque(false);
                    setSize(gameFrame.getSize());
                    
                    // Start animation timer
                    animationTimer = new Timer(50, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            frame++;
                            repaint();
                            
                            // End animation after 3 seconds
                            if (frame > 60) {
                                animationTimer.stop();
                                if (animationPanelHolder[0] != null) {
                                    gameFrame.getLayeredPane().remove(animationPanelHolder[0]);
                                    gameFrame.repaint();
                                }
                            }
                        }
                    });
                    animationTimer.start();
                }
                
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Create fade effect
                    float alpha = Math.max(0, 1.0f - (frame / 60.0f));
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    
                    // Background with gradient
                    GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(0, 0, 0, (int)(100 * alpha)),
                        getWidth(), getHeight(), new Color(50, 50, 100, (int)(150 * alpha))
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    
                    // Animated title text
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    Font titleFont = new Font("Arial", Font.BOLD, 48 + (int)(20 * Math.sin(frame * 0.2)));
                    g2d.setFont(titleFont);
                    
                    // Rainbow effect for text
                    float hue = (frame * 0.05f) % 1.0f;
                    Color textColor = Color.getHSBColor(hue, 0.8f, 1.0f);
                    g2d.setColor(textColor);
                    
                    String title = "♚ CHESS GAME ♚";
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(title)) / 2;
                    int y = getHeight() / 2 - 50;
                    
                    // Add shadow effect
                    g2d.setColor(new Color(0, 0, 0, (int)(100 * alpha)));
                    g2d.drawString(title, x + 3, y + 3);
                    g2d.setColor(textColor);
                    g2d.drawString(title, x, y);
                    
                    // Animated subtitle
                    g2d.setFont(new Font("Arial", Font.ITALIC, 24));
                    g2d.setColor(new Color(255, 255, 255, (int)(200 * alpha)));
                    String subtitle = "Ready to Play!";
                    int subX = (getWidth() - g2d.getFontMetrics().stringWidth(subtitle)) / 2;
                    int subY = y + 60;
                    g2d.drawString(subtitle, subX, subY);
                    
                    // Animated particles
                    for (int i = 0; i < 20; i++) {
                        float particleFrame = frame + i * 3;
                        int px = (int)(50 + i * 60 + 30 * Math.sin(particleFrame * 0.1));
                        int py = (int)(100 + 50 * Math.cos(particleFrame * 0.15));
                        int size = (int)(5 + 3 * Math.sin(particleFrame * 0.2));
                        
                        g2d.setColor(Color.getHSBColor((i * 0.1f + frame * 0.02f) % 1.0f, 0.6f, 1.0f));
                        g2d.fillOval(px, py, size, size);
                    }
                }
            };
            
            animationPanelHolder[0] = animationPanel;
            
            // Add to layered pane for overlay effect
            gameFrame.getLayeredPane().add(animationPanel, JLayeredPane.PALETTE_LAYER);
        });
    }
    
    /**
     * Show beautiful game end animation
     */
    private void showGameEndAnimation() {
        SwingUtilities.invokeLater(() -> {
            // Create victory animation overlay
            final JPanel[] victoryPanelHolder = new JPanel[1];
            
            JPanel victoryPanel = new JPanel() {
                private int frame = 0;
                private Timer animationTimer;
                
                {
                    setOpaque(false);
                    setSize(gameFrame.getSize());
                    
                    // Start animation timer
                    animationTimer = new Timer(80, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            frame++;
                            repaint();
                            
                            // End animation after 5 seconds
                            if (frame > 60) {
                                animationTimer.stop();
                                if (victoryPanelHolder[0] != null) {
                                    gameFrame.getLayeredPane().remove(victoryPanelHolder[0]);
                                    gameFrame.repaint();
                                }
                            }
                        }
                    });
                    animationTimer.start();
                }
                
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Create celebration effect
                    float alpha = Math.min(1.0f, frame / 20.0f);
                    if (frame > 40) alpha = Math.max(0, 1.0f - ((frame - 40) / 20.0f));
                    
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.8f));
                    
                    // Celebration background
                    GradientPaint celebrationGradient = new GradientPaint(
                        0, 0, new Color(255, 215, 0, (int)(150 * alpha)), // Gold
                        getWidth(), getHeight(), new Color(255, 140, 0, (int)(100 * alpha)) // Orange
                    );
                    g2d.setPaint(celebrationGradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    
                    // Animated victory text
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    Font victoryFont = new Font("Arial", Font.BOLD, 56 + (int)(15 * Math.sin(frame * 0.3)));
                    g2d.setFont(victoryFont);
                    
                    String victoryText = "🎉 GAME OVER! 🎉";
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(victoryText)) / 2;
                    int y = getHeight() / 2;
                    
                    // Glowing effect
                    for (int glow = 8; glow > 0; glow--) {
                        g2d.setColor(new Color(255, 255, 0, (int)(30 * alpha)));
                        g2d.drawString(victoryText, x + glow, y + glow);
                    }
                    
                    g2d.setColor(new Color(255, 215, 0)); // Gold color
                    g2d.drawString(victoryText, x, y);
                    
                    // Fireworks effect
                    for (int i = 0; i < 15; i++) {
                        drawFirework(g2d, 
                            100 + i * 80, 
                            150 + (int)(50 * Math.sin((frame + i * 5) * 0.2)),
                            frame + i * 4,
                            alpha);
                    }
                    
                    // Confetti effect
                    for (int i = 0; i < 50; i++) {
                        drawConfetti(g2d, i, frame, alpha);
                    }
                }
                
                private void drawFirework(Graphics2D g2d, int centerX, int centerY, int animFrame, float alpha) {
                    int explosion = Math.max(0, animFrame - 20);
                    if (explosion > 0) {
                        int radius = Math.min(explosion * 3, 60);
                        
                        for (int angle = 0; angle < 360; angle += 15) {
                            double radians = Math.toRadians(angle);
                            int x = centerX + (int)(radius * Math.cos(radians));
                            int y = centerY + (int)(radius * Math.sin(radians));
                            
                            Color sparkColor = Color.getHSBColor((angle / 360.0f + animFrame * 0.02f) % 1.0f, 1.0f, 1.0f);
                            g2d.setColor(new Color(sparkColor.getRed(), sparkColor.getGreen(), sparkColor.getBlue(), 
                                (int)(150 * alpha * Math.max(0, 1 - explosion / 30.0))));
                            g2d.fillOval(x - 3, y - 3, 6, 6);
                        }
                    }
                }
                
                private void drawConfetti(Graphics2D g2d, int index, int animFrame, float alpha) {
                    int x = (index * 25) % getWidth();
                    int y = ((animFrame + index * 7) * 5) % getHeight();
                    int size = 4 + (index % 4);
                    
                    Color confettiColor = Color.getHSBColor((index * 0.1f) % 1.0f, 0.8f, 1.0f);
                    g2d.setColor(new Color(confettiColor.getRed(), confettiColor.getGreen(), confettiColor.getBlue(), 
                        (int)(180 * alpha)));
                    
                    // Rotating confetti
                    g2d.rotate(Math.toRadians(animFrame * 5 + index * 45), x, y);
                    g2d.fillRect(x - size/2, y - size/2, size, size * 2);
                    g2d.rotate(-Math.toRadians(animFrame * 5 + index * 45), x, y);
                }
            };
            
            victoryPanelHolder[0] = victoryPanel;
            
            // Add to layered pane for overlay effect
            gameFrame.getLayeredPane().add(victoryPanel, JLayeredPane.PALETTE_LAYER);
        });
    }
}
