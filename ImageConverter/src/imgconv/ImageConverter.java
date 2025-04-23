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
    private ExecutorService executorService = Executors.newFixedThreadPool(5); // 동시에 처리할 스레드 개수 설정

    public ImageConverter() {
        setTitle("이미지 변환기");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // 배경 그라데이션 패널
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                Color colorStart = new Color(135, 206, 235); // 하늘색
                Color colorEnd = Color.WHITE;
                GradientPaint gradient = new GradientPaint(0, 0, colorStart, getWidth(), 0, colorEnd);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        backgroundPanel.setLayout(new BorderLayout());

        // 확장자 선택 버튼 그룹
        JPanel formatPanel = new JPanel();
        formatPanel.setBorder(new TitledBorder("변환할 확장자"));
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

        // 로그 기록 영역 (TextArea에 드래그 앤 드롭 기능 추가)
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
                            convertImages(); // TextArea에 드롭해도 변환 시작
                        } catch (Exception e) {
                            logTextArea.append("TextArea에 파일 드롭 중 오류 발생: " + e.getMessage() + "\n");
                        }
                        return;
                    }
                }
                event.dropComplete(false);
            }
        });
        JScrollPane logScrollPane = new JScrollPane(logTextArea);



        // 아이콘 설정
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            java.io.InputStream inputStream = classLoader.getResourceAsStream("icon.png");
            if (inputStream != null) {
                setIconImage(ImageIO.read(inputStream));
                inputStream.close();
            } else {
                logTextArea.append("아이콘 파일(icon.png)을 찾을 수 없습니다 (JAR 내부).\n");
            }
        } catch (IOException e) {
            logTextArea.append("아이콘 파일 로딩 중 오류 발생: " + e.getMessage() + "\n");
        }
        
        // 파일 선택 및 정보 버튼
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        selectButton = new JButton("사진 선택");
        infoButton = new JButton("?");
        buttonPanel.add(selectButton);
        buttonPanel.add(infoButton);

        // JFrame에 드래그 앤 드롭 기능 다시 활성화
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
                            convertImages(); // JFrame에 드롭해도 변환 시작
                        } catch (Exception e) {
                            logTextArea.append("JFrame에 파일 드롭 중 오류 발생: " + e.getMessage() + "\n");
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

        // 프로그램 종료 시 스레드 풀 정리
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                executorService.shutdown(); // 더 이상 새로운 작업은 받지 않음
                try {
                    if (!executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) { // 5초 동안 작업 완료 대기
                        executorService.shutdownNow(); // 타임아웃 시 강제 종료 시도
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow(); // 인터럽트 발생 시 강제 종료
                    Thread.currentThread().interrupt();
                } finally {
                    System.exit(0);
                }
            }
        });
    }

    private void convertImages() {
        if (selectedFiles == null || selectedFiles.length == 0) {
            logTextArea.append("선택된 파일이 없습니다.\n");
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
            logTextArea.append("변환할 확장자를 선택해주세요.\n");
            return;
        }

        String finalTargetFormat = targetFormat; // 스레드에서 사용하기 위해 final 변수로 만듦

        for (File file : selectedFiles) {
            String fileName = file.getName();
            String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

            if (!fileExtension.equals("jpg") && !fileExtension.equals("jpeg") &&
                !fileExtension.equals("png") && !fileExtension.equals("h264") && !fileExtension.equals("ico") && !fileExtension.equals("heic")) {
                logTextArea.append(fileName + ": 변환 불가, jpg, png, H264, ico, heic만 가능합니다.\n");
                continue;
            }

            File finalFile = file; // 스레드에서 사용하기 위해 final 변수로 만듦
            String processingMessage = finalFile.getName() + " [변환중]\n";
            SwingUtilities.invokeLater(() -> logTextArea.append(processingMessage)); // UI 업데이트는 EventQueue에서

            executorService.submit(() -> {
                FileOutputStream fos = null;
                try {
                    BufferedImage originalImage = ImageIO.read(finalFile);
                    System.out.println(finalFile.getName() + " - ImageIO.read() 결과: " + originalImage); // 로그 추가
                    if (originalImage == null && !fileExtension.equals("h264") && !fileExtension.equals("ico")) {
                        SwingUtilities.invokeLater(() -> logTextArea.append("파일을 이미지로 읽을 수 없습니다: " + finalFile.getName() + "\n"));
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
                                SwingUtilities.invokeLater(() -> logTextArea.append(finalFile.getName() + " -> " + outputFile.getName() + " (ICO) 변환 완료\n"));
                            } catch (IOException e) {
                                SwingUtilities.invokeLater(() -> logTextArea.append(finalFile.getName() + " -> " + outputFile.getName() + " (ICO) 변환 실패: " + e.getMessage() + "\n"));
                            }
                        } else {
                            SwingUtilities.invokeLater(() -> logTextArea.append(finalFile.getName() + ": ICO 변환을 위한 이미지 데이터를 읽을 수 없습니다.\n"));
                        }
                    } else if (!fileExtension.equals("h264") && originalImage != null) {
                        BufferedImage rgbImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                        Graphics2D g = rgbImage.createGraphics();
                        g.drawImage(originalImage, 0, 0, null);
                        g.dispose();
                        ImageIO.write(rgbImage, finalTargetFormat, outputFile);
                        SwingUtilities.invokeLater(() -> logTextArea.append(finalFile.getName() + " -> " + outputFile.getName() + " 변환 완료\n"));
                    } else if (fileExtension.equals("h264")) {
                        SwingUtilities.invokeLater(() -> logTextArea.append(finalFile.getName() + " -> " + outputFile.getName() + ": H264 파일은 이미지 변환을 지원하지 않습니다.\n"));
                    }

                } catch (IOException e) {
                    String errorMessage = "이미지 변환 중 오류가 발생했습니다: " + finalFile.getName() + " - " + e.getMessage();
                    SwingUtilities.invokeLater(() -> logTextArea.append(errorMessage + "\n"));
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    SwingUtilities.invokeLater(() -> logTextArea.append("지원하지 않는 이미지 형식입니다: " + finalFile.getName() + " - " + e.getMessage() + "\n"));
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            SwingUtilities.invokeLater(() -> logTextArea.append("ICO 파일 스트림 닫기 실패: " + e.getMessage() + "\n"));
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