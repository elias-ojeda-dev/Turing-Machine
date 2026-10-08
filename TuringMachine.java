import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Stream;


public final class TuringMachine extends JFrame {
    private static final long serialVersionUID = 1L;
    private transient final TuringMachineEngine engine = new TuringMachineEngine();

    private JTextField inputField;
    private JButton startButton, stepButton, autoButton, resetButton;
    private JPanel tapePanel;
    private JScrollPane tapeScrollPane;
    private JLabel stateLabel, resultLabel;
    private JTextArea descriptionArea;
    private JPanel stateIndicator;
    private JProgressBar progressBar;
    private javax.swing.Timer animationTimer;

    private boolean simulationActive;
    private boolean simulationCompleted;
    private boolean graphicMode;

    @SuppressWarnings("this-escape")
    public TuringMachine(){
        initializeComponents();
        setupLayout();
        resetSimulation();
    }

    private void initializeComponents(){
        setTitle("Maquina de Turing - Lenguaje {0^n1^n | n>=1}");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        inputField = new JTextField(18);
        inputField.setFont(new Font("Monospaced", Font.PLAIN, 16));

        startButton = new JButton("Iniciar");
        stepButton = new JButton("Paso");
        autoButton = new JButton("Auto");
        resetButton = new JButton("Reset");
        Stream.of(startButton, stepButton, autoButton, resetButton)
                .forEach(b -> {
                    b.setFocusPainted(false);
                    b.setFont(new Font("Segoe UI", Font.BOLD, 13));
                });
        stepButton.setEnabled(false);
        autoButton.setEnabled(false);

        tapePanel = new JPanel();
        tapePanel.setLayout(new BoxLayout(tapePanel, BoxLayout.Y_AXIS));
        tapePanel.setBackground(Color.WHITE);

        tapeScrollPane = new JScrollPane(tapePanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tapeScrollPane.getViewport().setBackground(Color.WHITE);
        tapeScrollPane.setBorder(BorderFactory.createEmptyBorder());

        stateLabel = new JLabel();
        stateLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        resultLabel = new JLabel(" ");
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        descriptionArea = new JTextArea(3, 40);
        descriptionArea.setEditable(false);
        descriptionArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));

        stateIndicator = new JPanel();
        stateIndicator.setPreferredSize(new Dimension(14, 14));
        stateIndicator.setBackground(Color.DARK_GRAY);
        stateIndicator.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        setupListeners();
    }

    private void setupLayout(){
        setLayout(new BorderLayout(0, 5));

        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(new Color(245, 245, 245));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.gridy = 0;

        gbc.gridx = 0;
        JLabel inputLbl = new JLabel("Entrada");
        inputLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        topPanel.add(inputLbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(inputField, gbc);

        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 2;
        topPanel.add(startButton, gbc);

        gbc.gridx = 3;
        topPanel.add(stepButton, gbc);

        gbc.gridx = 4;
        topPanel.add(autoButton, gbc);

        gbc.gridx = 5;
        topPanel.add(resetButton, gbc);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));
        infoPanel.setBackground(Color.WHITE);

        JPanel stateRow = new JPanel();
        stateRow.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        stateRow.setOpaque(false);
        stateRow.add(new JLabel("Estado:"));
        stateRow.add(stateIndicator);
        stateRow.add(stateLabel);
        stateRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setBorder(BorderFactory.createEmptyBorder());
        descriptionScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        resultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        stateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(stateRow);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(descriptionScroll);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(progressBar);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(resultLabel);

        JPanel centralPanel = new JPanel(new BorderLayout());
        centralPanel.add(infoPanel, BorderLayout.NORTH);
        centralPanel.add(tapeScrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(centralPanel, BorderLayout.CENTER);
    }

    private void setupListeners(){
        startButton.addActionListener(e -> startSimulation());
        stepButton.addActionListener(e -> nextStep());
        resetButton.addActionListener(e -> resetSimulation());
        autoButton.addActionListener(e -> runAuto());

        animationTimer = new javax.swing.Timer(1500, (ActionEvent e) -> {
            if(simulationActive && !simulationCompleted){
                nextStep();
            }else{
                animationTimer.stop();
                autoButton.setText("Auto");
            }
        });
    }

    private void startSimulation(){
        String input = inputField.getText().trim();

        if(input.isEmpty()){
            JOptionPane.showMessageDialog(this,
                    "Por favor ingrese una cadena.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if(!input.matches("[01]+")){
            JOptionPane.showMessageDialog(this,
                    "La cadena solo puede contener 0s y 1s.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        graphicMode = input.length() <= 10;

        if(!graphicMode){
            JOptionPane.showMessageDialog(this,
                    "Cadena de " + input.length() + " caracteres.\n" +
                            "Se ejecutara en modo no grafico.\n" +
                            "Los resultados se guardaran en el archivo de log.",
                    "Modo No Grafico", JOptionPane.INFORMATION_MESSAGE);
        }

        engine.start(input, graphicMode);
        stateIndicator.setBackground(new Color(52, 73, 94));

        simulationActive = true;
        simulationCompleted = false;

        if(graphicMode){
            updateUIState();
            stepButton.setEnabled(true);
            autoButton.setEnabled(true);
            progressBar.setVisible(false);
        }else{
            runHeadlessMode();
        }

        startButton.setEnabled(false);
    }

    private void runHeadlessMode(){
        autoButton.setEnabled(false);
        stepButton.setEnabled(false);
        progressBar.setVisible(true);

        stateLabel.setText("Ejecutando en modo no grafico...");
        stateIndicator.setBackground(new Color(255, 165, 0));
        descriptionArea.setText("Procesando cadena de " + inputField.getText().length() + " caracteres...");
        tapePanel.removeAll();
        JLabel procesandoLabel = new JLabel("Procesando en modo no grafico", SwingConstants.CENTER);
        procesandoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        procesandoLabel.setForeground(Color.BLUE.darker());
        tapePanel.add(procesandoLabel);
        tapePanel.revalidate();
        tapePanel.repaint();

        SwingWorker<Void, String> worker = new SwingWorker<Void, String>(){
            @Override
            protected Void doInBackground(){
                while(simulationActive && !simulationCompleted){
                    StepStatus status = engine.step();

                    if(engine.getStep() % 100 == 0){
                        publish("Paso: " + engine.getStep() + ", Estado: " + engine.getCurrentState());
                    }

                    if(status != StepStatus.RUNNING){
                        final StepStatus finalStatus = status;
                        SwingUtilities.invokeLater(() -> handleResult(finalStatus));
                        break;
                    }
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks){
                if(!chunks.isEmpty()){
                    descriptionArea.setText(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done(){
                stateLabel.setText("Estado: " + engine.getCurrentState());
                if(simulationCompleted){
                    descriptionArea.setText("Simulacion completada en " + engine.getStep() + " pasos.");
                }
                progressBar.setVisible(false);
            }
        };

        worker.execute();
    }

    private void nextStep(){
        if(!simulationActive || simulationCompleted){
            return;
        }

        StepStatus status = engine.step();
        handleResult(status);

        if(graphicMode){
            updateUIState();
        }
    }

    private void handleResult(StepStatus stepOutcome){
        if(stepOutcome == StepStatus.RUNNING){
            return;
        }

        simulationCompleted = true;
        simulationActive = false;

        if(animationTimer.isRunning()){
            animationTimer.stop();
        }
        autoButton.setText("Auto");
        autoButton.setEnabled(false);
        stepButton.setEnabled(false);

        switch(stepOutcome){
            case ACCEPTED:
                resultLabel.setText("Cadena aceptada (" + engine.getStep() + " pasos)");
                resultLabel.setForeground(Color.GREEN);
                stateIndicator.setBackground(new Color(46, 204, 113));
                break;
            case LOOP_LIMIT:
                resultLabel.setText("Cadena rechazada (Bucle infinito)");
                resultLabel.setForeground(Color.RED);
                stateIndicator.setBackground(new Color(255, 165, 0));
                break;
            default:
                resultLabel.setText("Cadena rechazada");
                resultLabel.setForeground(Color.RED);
                stateIndicator.setBackground(new Color(231, 76, 60));
                break;
        }

        stateLabel.setText("Estado: " + engine.getCurrentState());
    }

    private void updateUIState(){
        if(!graphicMode || engine.getTape() == null){
            return;
        }

        stateLabel.setText("Estado: " + engine.getCurrentState());
        boolean aceptado = engine.getCurrentState().equals(engine.getAcceptState());
        if(aceptado){
            stateLabel.setForeground(Color.GREEN.darker());
            stateIndicator.setBackground(new Color(46, 204, 113));
        }else{
            stateLabel.setForeground(Color.BLACK);
            stateIndicator.setBackground(new Color(52, 73, 94));
        }

        Tape tape = engine.getTape();
        List<Character> symbols = tape.getSymbols();
        int head = tape.getHeadPosition();
        char blank = engine.getBlankSymbol();

        StringBuilder desc = new StringBuilder();
        for(int i=0; i<head && i<symbols.size(); i++){
            desc.append(symbols.get(i));
        }
        desc.append(engine.getCurrentState());
        if(head < symbols.size()){
            desc.append(symbols.get(head));
        }
        for(int i=head+1; i<symbols.size() && i<head+8; i++){
            if(symbols.get(i) != blank || i <= head + 3){
                desc.append(symbols.get(i));
            }
        }
        descriptionArea.setText("Descripcion instantanea: " + desc.toString());

        drawTape(symbols, head);
    }

    private void drawTape(List<Character> symbols, int headPosition){
        tapePanel.removeAll();

        int startIndex = Math.max(0, headPosition - 7);
        int endIndex = Math.min(symbols.size(), startIndex + 15);

        JPanel cells = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        cells.setOpaque(false);

        for(int i=startIndex; i<endIndex; i++){
            JLabel cell = new JLabel(String.valueOf(symbols.get(i)), SwingConstants.CENTER);
            cell.setPreferredSize(new Dimension(40, 40));
            cell.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 2));
            cell.setOpaque(true);

            if(i == headPosition){
                cell.setBackground(new Color(255, 235, 59));
                cell.setFont(new Font("Monospaced", Font.BOLD, 14));
            }else{
                cell.setBackground(new Color(245, 245, 245));
                cell.setFont(new Font("Monospaced", Font.PLAIN, 12));
            }

            cells.add(cell);
        }

        JPanel indicatorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        indicatorPanel.setOpaque(false);
        for(int i=startIndex; i<endIndex; i++){
            JLabel indicator = new JLabel(" ", SwingConstants.CENTER);
            indicator.setPreferredSize(new Dimension(40, 12));
            if(i == headPosition){
                indicator.setText("^");
                indicator.setForeground(Color.RED);
                indicator.setFont(new Font("Arial", Font.BOLD, 12));
                indicator.setHorizontalAlignment(SwingConstants.CENTER);
            }
            indicatorPanel.add(indicator);
        }

        tapePanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        tapePanel.add(cells, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 4, 0);
        tapePanel.add(indicatorPanel, gbc);
        tapePanel.revalidate();
        tapePanel.repaint();
    }

    private void runAuto(){
        if(!graphicMode){
            JOptionPane.showMessageDialog(this,
                    "El modo automatico no esta disponible para cadenas largas.\n" +
                            "La simulacion se ejecuta automaticamente en modo no grafico.",
                    "Informacion", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if(animationTimer.isRunning()){
            animationTimer.stop();
            autoButton.setText("Auto");
        }else{
            if(!simulationActive){
                startSimulation();
            }
            if(simulationActive && !simulationCompleted){
                animationTimer.start();
                autoButton.setText("Pausar");
            }
        }
    }

    private void resetSimulation(){
        simulationActive = false;
        simulationCompleted = false;
        if(animationTimer != null && animationTimer.isRunning()){
            animationTimer.stop();
        }

        engine.reset();

        stateLabel.setText("Estado: q0");
        stateLabel.setForeground(Color.BLACK);
        stateIndicator.setBackground(new Color(128, 128, 128));
        descriptionArea.setText("Descripcion instantanea: ");
        resultLabel.setText(" ");

        tapePanel.removeAll();
        tapePanel.revalidate();
        tapePanel.repaint();

        startButton.setEnabled(true);
        stepButton.setEnabled(false);
        autoButton.setEnabled(false);
        autoButton.setText("Auto");
        progressBar.setVisible(false);
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            try{
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }catch(Exception e){
                e.printStackTrace();
            }

            new TuringMachine().setVisible(true);
        });
    }
}
