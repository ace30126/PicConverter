package imgconv;

import net.sf.image4j.codec.ico.ICOEncoder;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;

public class ImageConverter extends JFrame {

    private JRadioButton pngButton;
    private JRadioButton jpgButton;
    private JRadioButton icoButton;
    private JTextArea logTextArea;
    private JButton selectButton;
    private JButton infoButton;
    private File[] selectedFiles;
    private ExecutorService executorService = Executors.newFixedThreadPool(5); // ���ÿ� ó���� ������ ���� ����

    public ImageConverter() {
        setTitle("�̹��� ��ȯ��");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // ��� �׶��̼� �г�
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                Color colorStart = new Color(135, 206, 235); // �ϴû�
                Color colorEnd = Color.WHITE;
                GradientPaint gradient = new GradientPaint(0, 0, colorStart, getWidth(), 0, colorEnd);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        backgroundPanel.setLayout(new BorderLayout());

        // Ȯ���� ���� ��ư �׷�
        JPanel formatPanel = new JPanel();
        formatPanel.setBorder(new TitledBorder("��ȯ�� Ȯ����"));
        pngButton = new JRadioButton("to PNG");
        jpgButton = new JRadioButton("to JPEG");
        icoButton = new JRadioButton("to ICO");
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(pngButton);
        buttonGroup.add(jpgButton);
        buttonGroup.add(icoButton);
        formatPanel.add(pngButton);
        formatPanel.add(jpgButton);
        formatPanel.add(icoButton);

        // �α� ��� ���� (TextArea�� �巡�� �� ��� ��� �߰�)
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        new DropTarget(logTextArea, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent event) {
                event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                Transferable transferable = event.getTransferable();
                DataFlavor[] flavors = transferable.getTransferDataFlavors();
                for (DataFlavor flavor : flavors) {
                    if (flavor.isFlavorJavaFileListType()) {
                        try {
                            List<File> droppedFiles = (List<File>) transferable.getTransferData(flavor);
                            selectedFiles = droppedFiles.toArray(new File[0]);
                            convertImages(); // TextArea�� ����ص� ��ȯ ����
                        } catch (Exception e) {
                            logTextArea.append("TextArea�� ���� ��� �� ���� �߻�: " + e.getMessage() + "\n");
                        }
                        return;
                    }
                }
                event.dropComplete(false);
            }
        });
        JScrollPane logScrollPane = new JScrollPane(logTextArea);



        // ������ ����
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            java.io.InputStream inputStream = classLoader.getResourceAsStream("icon.png");
            if (inputStream != null) {
                setIconImage(ImageIO.read(inputStream));
                inputStream.close();
            } else {
                logTextArea.append("������ ����(icon.png)�� ã�� �� �����ϴ� (JAR ����).\n");
            }
        } catch (IOException e) {
            logTextArea.append("������ ���� �ε� �� ���� �߻�: " + e.getMessage() + "\n");
        }
        
        // ���� ���� �� ���� ��ư
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        selectButton = new JButton("���� ����");
        infoButton = new JButton("?");
        buttonPanel.add(selectButton);
        buttonPanel.add(infoButton);

        // JFrame�� �巡�� �� ��� ��� �ٽ� Ȱ��ȭ
        setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent event) {
                event.acceptDrop(DnDConstants.ACTION_COPY);
                Transferable transferable = event.getTransferable();
                DataFlavor[] flavors = transferable.getTransferDataFlavors();
                for (DataFlavor flavor : flavors) {
                    if (flavor.isFlavorJavaFileListType()) {
                        try {
                            List<File> files = (List<File>) transferable.getTransferData(flavor);
                            selectedFiles = files.toArray(new File[0]);
                            convertImages(); // JFrame�� ����ص� ��ȯ ����
                        } catch (Exception e) {
                            logTextArea.append("JFrame�� ���� ��� �� ���� �߻�: " + e.getMessage() + "\n");
                        }
                        return;
                    }
                }
                event.dropComplete(false);
            }
        }));

        backgroundPanel.add(formatPanel, BorderLayout.NORTH);
        backgroundPanel.add(logScrollPane, BorderLayout.CENTER);
        backgroundPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(backgroundPanel);
        setVisible(true);

        // ���α׷� ���� �� ������ Ǯ ����
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                executorService.shutdown(); // �� �̻� ���ο� �۾��� ���� ����
                try {
                    if (!executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) { // 5�� ���� �۾� �Ϸ� ���
                        executorService.shutdownNow(); // Ÿ�Ӿƿ� �� ���� ���� �õ�
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow(); // ���ͷ�Ʈ �߻� �� ���� ����
                    Thread.currentThread().interrupt();
                } finally {
                    System.exit(0);
                }
            }
        });
    }

    private void convertImages() {
        if (selectedFiles == null || selectedFiles.length == 0) {
            logTextArea.append("���õ� ������ �����ϴ�.\n");
            return;
        }

        String targetFormat = null;
        if (pngButton.isSelected()) {
            targetFormat = "png";
        } else if (jpgButton.isSelected()) {
            targetFormat = "jpg";
        } else if (icoButton.isSelected()) {
            targetFormat = "ico";
        } else {
            logTextArea.append("��ȯ�� Ȯ���ڸ� �������ּ���.\n");
            return;
        }

        String finalTargetFormat = targetFormat; // �����忡�� ����ϱ� ���� final ������ ����

        for (File file : selectedFiles) {
            String fileName = file.getName();
            String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

            if (!fileExtension.equals("jpg") && !fileExtension.equals("jpeg") &&
                !fileExtension.equals("png") && !fileExtension.equals("h264") && !fileExtension.equals("ico") && !fileExtension.equals("heic")) {
                logTextArea.append(fileName + ": ��ȯ �Ұ�, jpg, png, H264, ico, heic�� �����մϴ�.\n");
                continue;
            }

            File finalFile = file; // �����忡�� ����ϱ� ���� final ������ ����
            String processingMessage = finalFile.getName() + " [��ȯ��]\n";
            SwingUtilities.invokeLater(() -> logTextArea.append(processingMessage)); // UI ������Ʈ�� EventQueue����

            executorService.submit(() -> {
                FileOutputStream fos = null;
                try {
                    BufferedImage originalImage = ImageIO.read(finalFile);
                    System.out.println(finalFile.getName() + " - ImageIO.read() ���: " + originalImage); // �α� �߰�
                    if (originalImage == null && !fileExtension.equals("h264") && !fileExtension.equals("ico")) {
                        SwingUtilities.invokeLater(() -> logTextArea.append("������ �̹����� ���� �� �����ϴ�: " + finalFile.getName() + "\n"));
                        return;
                    }

                    String originalName = fileName.substring(0, fileName.lastIndexOf("."));
                    File outputFile = new File(finalFile.getParent(), originalName + "_conv." + finalTargetFormat);

                    if (finalTargetFormat.equals("ico")) {
                        List<BufferedImage> icoImages = new ArrayList<>();
                        if (originalImage != null) {
                            icoImages.add(originalImage);
                            try {
                                fos = new FileOutputStream(outputFile);
                                ICOEncoder.write(icoImages, fos);
                                SwingUtilities.invokeLater(() -> logTextArea.append(finalFile.getName() + " -> " + outputFile.getName() + " (ICO) ��ȯ �Ϸ�\n"));
                            } catch (IOException e) {
                                SwingUtilities.invokeLater(() -> logTextArea.append(finalFile.getName() + " -> " + outputFile.getName() + " (ICO) ��ȯ ����: " + e.getMessage() + "\n"));
                            }
                        } else {
                            SwingUtilities.invokeLater(() -> logTextArea.append(finalFile.getName() + ": ICO ��ȯ�� ���� �̹��� �����͸� ���� �� �����ϴ�.\n"));
                        }
                    } else if (!fileExtension.equals("h264") && originalImage != null) {
                        BufferedImage rgbImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                        Graphics2D g = rgbImage.createGraphics();
                        g.drawImage(originalImage, 0, 0, null);
                        g.dispose();
                        ImageIO.write(rgbImage, finalTargetFormat, outputFile);
                        SwingUtilities.invokeLater(() -> logTextArea.append(finalFile.getName() + " -> " + outputFile.getName() + " ��ȯ �Ϸ�\n"));
                    } else if (fileExtension.equals("h264")) {
                        SwingUtilities.invokeLater(() -> logTextArea.append(finalFile.getName() + " -> " + outputFile.getName() + ": H264 ������ �̹��� ��ȯ�� �������� �ʽ��ϴ�.\n"));
                    }

                } catch (IOException e) {
                    String errorMessage = "�̹��� ��ȯ �� ������ �߻��߽��ϴ�: " + finalFile.getName() + " - " + e.getMessage();
                    SwingUtilities.invokeLater(() -> logTextArea.append(errorMessage + "\n"));
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    SwingUtilities.invokeLater(() -> logTextArea.append("�������� �ʴ� �̹��� �����Դϴ�: " + finalFile.getName() + " - " + e.getMessage() + "\n"));
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            SwingUtilities.invokeLater(() -> logTextArea.append("ICO ���� ��Ʈ�� �ݱ� ����: " + e.getMessage() + "\n"));
                        }
                    }
                    SwingUtilities.invokeLater(() -> logTextArea.setCaretPosition(logTextArea.getDocument().getLength()));
                }
            });
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ImageConverter::new);
    }
}