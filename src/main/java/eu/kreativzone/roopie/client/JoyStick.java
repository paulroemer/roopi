package eu.kreativzone.roopie.client;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class JoyStick extends JLabel implements MouseMotionListener {
	Dimension size = new Dimension(100, 100);
	ArrayList<StickListener> listeners = new ArrayList<StickListener>();
	private int x = 50;
	private int y = 50;
	private final StickEvent sv = new StickEvent();

	public JoyStick(Color color) {
		setBackground(color);
		setOpaque(true);
		setBorder(BorderFactory.createLineBorder(Color.black));
		addMouseMotionListener(this);
	}

	public Dimension getMinimumSize() {
		return size;
	}

	public Dimension getPreferredSize() {
		return size;
	}

	public void mouseMoved(MouseEvent ev) {
	}

	public void mouseDragged(MouseEvent ev) {
		x = ev.getX();
		y = ev.getY();
		if (x < 0)
			x = 0;
		if (x > size.width)
			x = size.width;
		if (y < 0)
			y = 0;
		if (y > size.height)
			y = size.height;
		repaint();
		sv.X = x;
		sv.Y = y;
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).stickMoved(sv);
	}

	public void zero() {
		x = 50;
		y = 50;
		repaint();
	}

	public void addStickListener(StickListener x) {
		listeners.add(x);
	}

	public void removeStickListener(StickListener x) {
		listeners.remove(x);
	}

	public void paint(Graphics g) {
		super.paint(g);
		g.drawLine(50, 0, 50, 100);
		g.drawLine(0, 50, 100, 50);
		Color c = g.getColor();
		g.setColor(Color.RED);
		g.fillOval(x - 7, y - 7, 14, 14);
		g.setColor(c);
	}
}
