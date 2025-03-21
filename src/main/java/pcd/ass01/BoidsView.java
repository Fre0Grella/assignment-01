package pcd.ass01;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

public class BoidsView implements ChangeListener {

	private JFrame frame;
	private BoidsPanel boidsPanel;
	private JSlider cohesionSlider, separationSlider, alignmentSlider;
	private BoidsModel model;
	private BoidsSimulator sim;
	private int width, height;
	private JButton startStopButton,resetButton;
	private JTextField boidInputField;
	
	public BoidsView(BoidsModel model, BoidsSimulator simulator, int width, int height) {
		this.model = model;
		this.sim = simulator;
		this.width = width;
		this.height = height;
		
		frame = new JFrame("Boids Simulation");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel cp = new JPanel();
		LayoutManager layout = new BorderLayout();
		cp.setLayout(layout);

        boidsPanel = new BoidsPanel(this, model);
		cp.add(BorderLayout.CENTER, boidsPanel);

		JPanel controlPanel = new JPanel();

		boidInputField = new JTextField(String.valueOf(BoidsSimulation.DEFAULT_BOIDS), 5);
		resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetSimulation();
			}
		});
		startStopButton = new JButton("Start/Stop");
		startStopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleSimulation();
			}
		});

		controlPanel.add(new JLabel("Num Boids:"));
		controlPanel.add(boidInputField);
		controlPanel.add(resetButton);
		controlPanel.add(startStopButton);
		cp.add(BorderLayout.NORTH, controlPanel);

        JPanel slidersPanel = new JPanel();
        
        cohesionSlider = makeSlider();
        separationSlider = makeSlider();
        alignmentSlider = makeSlider();
        
        slidersPanel.add(new JLabel("Separation"));
        slidersPanel.add(separationSlider);
        slidersPanel.add(new JLabel("Alignment"));
        slidersPanel.add(alignmentSlider);
        slidersPanel.add(new JLabel("Cohesion"));
        slidersPanel.add(cohesionSlider);
		        
		cp.add(BorderLayout.SOUTH, slidersPanel);

		frame.setContentPane(cp);	
		
        frame.setVisible(true);
	}

	private void resetSimulation() {
		sim.stopSimulation();
		try {
			int numBoids = Integer.parseInt(boidInputField.getText());
			model.initializeBoids(numBoids);
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(frame, "Invalid number of boids", "Error", JOptionPane.ERROR_MESSAGE);
		}

	}

	private JSlider makeSlider() {
		var slider = new JSlider(JSlider.HORIZONTAL, 0, 20, 10);        
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		Hashtable labelTable = new Hashtable<>();
		labelTable.put( 0, new JLabel("0") );
		labelTable.put( 10, new JLabel("1") );
		labelTable.put( 20, new JLabel("2") );
		slider.setLabelTable( labelTable );
		slider.setPaintLabels(true);
        slider.addChangeListener(this);
		return slider;
	}

	private void toggleSimulation() {
		sim.toogleSimulation();

	}
	
	public void update(int frameRate) {
		boidsPanel.setFrameRate(frameRate);
		boidsPanel.repaint();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == separationSlider) {
			var val = separationSlider.getValue();
			model.setSeparationWeight(0.1*val);
		} else if (e.getSource() == cohesionSlider) {
			var val = cohesionSlider.getValue();
			model.setCohesionWeight(0.1*val);
		} else {
			var val = alignmentSlider.getValue();
			model.setAlignmentWeight(0.1*val);
		}
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

}
