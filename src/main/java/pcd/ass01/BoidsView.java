package pcd.ass01;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Hashtable;

public class BoidsView implements ChangeListener {

    private final JFrame frame;
    private final BoidsPanel boidsPanel;
    private final JSlider cohesionSlider;
    private final JSlider separationSlider;
    private final JSlider alignmentSlider;
    private final BoidsModel model;
    private final BoidsSimulator sim;
    private final int width;
    private final int height;
    private final JButton startButton;
    private final JButton generateButton;
    private final JTextField boidInputField;
    private final JButton stopButton;
    private boolean firstTime = true;

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
        boidInputField = new JTextField(String.valueOf(0), 5);

        generateButton = new JButton("Generate");
        startButton = new JButton("Start");
        startButton.setEnabled(false);
        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);
        generateButton.addActionListener(e -> {
            generateButton.setEnabled(false);
            boidInputField.setEnabled(false);
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            generateSimulation();
            sim.startSimulation();
        });
        startButton.addActionListener(e -> {
            sim.startSimulation();
            generateButton.setEnabled(false);
            boidInputField.setEnabled(false);
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        });
        stopButton.addActionListener(e -> {
            generateButton.setEnabled(true);
            boidInputField.setEnabled(true);
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            sim.stopSimulation();
        });


        controlPanel.add(new JLabel("Num Boids:"));
        controlPanel.add(boidInputField);
        controlPanel.add(generateButton);
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
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

    private void generateSimulation() {
        try {
            int numBoids = Integer.parseInt(boidInputField.getText());
            model.initializeBoids(numBoids);
            if (firstTime) {
                firstTime = false;
                new Thread(sim::runSimulation).start();
            } else {
                sim.resetSimulation();
            }
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
        labelTable.put(0, new JLabel("0"));
        labelTable.put(10, new JLabel("1"));
        labelTable.put(20, new JLabel("2"));
        slider.setLabelTable(labelTable);
        slider.setPaintLabels(true);
        slider.addChangeListener(this);
        return slider;
    }

    public void update(int frameRate) {
        boidsPanel.setFrameRate(frameRate);
        boidsPanel.repaint();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == separationSlider) {
            var val = separationSlider.getValue();
            model.setSeparationWeight(0.1 * val);
        } else if (e.getSource() == cohesionSlider) {
            var val = cohesionSlider.getValue();
            model.setCohesionWeight(0.1 * val);
        } else {
            var val = alignmentSlider.getValue();
            model.setAlignmentWeight(0.1 * val);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
