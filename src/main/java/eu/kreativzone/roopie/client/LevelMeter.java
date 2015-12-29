package eu.kreativzone.roopie.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

class LevelMeter extends JLabel {
	private int value; // the value to display
	private int max, min;
	private float sv; // the scale value
	private int ov; // the offset value
	private static final Color plusColor = Color.GREEN;
	private static final Color minusColor = Color.RED;
	private static final Color background = Color.WHITE;
	private static final Color error = Color.GRAY;
	private Dimension minSize;

	LevelMeter(int min, int max, int height, int width, boolean label) {
		minSize = new Dimension(width, height);
		value = 0;
		this.max = max;
		this.min = min;
		sv = Math.round((max - min) / height); // pixels = value/sv;
		ov = Math.round(max / sv);
		setBackground(background);
		setOpaque(true);
		setBorder(BorderFactory.createLineBorder(Color.black));
	}

	LevelMeter(int min, int max, int height, int width) {
		this(min, max, height, width, false);
	}

	LevelMeter(int min, int max, int height) {
		this(min, max, height, 10, false);
	}

	public Dimension getMinimumSize() {
		return minSize;
	}

	public Dimension getPreferredSize() {
		return minSize;
	}

	public void setValue(int x) {
		value = x;
		repaint();
	}

	public int getValue() {
		return value;
	}

	public void paint(Graphics g) {
		super.paint(g);
		Dimension d = this.getSize();
		// ov is the "centre"
		Color c = g.getColor();
		int iv = Math.round(value / sv);
		if (value > max || value < min) {
			g.setColor(error);
			g.fillRect(1, 1, d.width - 2, d.height - 2);
		} else if (value >= 0) {
			g.setColor(plusColor);
			g.fillRect(1, ov - iv + 1, d.width - 2, iv);
		} else {
			g.setColor(minusColor);
			g.fillRect(1, ov, d.width - 2, -iv - 1);
		}
		g.setColor(c);
	}

	/**
	 * a test program
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame("Level Meter Test");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				JPanel p = new JPanel();
				p.setPreferredSize(new Dimension(100, 240));
				frame.getContentPane().add(p);
				final LevelMeter l = new LevelMeter(-50, 50, 100);
				JSlider slider = new JSlider(JSlider.VERTICAL, 0, 120, 0);
				p.add(slider);
				p.add(l);
				slider.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent ev) {
						JSlider source = (JSlider) ev.getSource();
						l.setValue(source.getValue() - 60);
					}
				});
				frame.pack();
				frame.setVisible(true);
			}
		});
	}
}
