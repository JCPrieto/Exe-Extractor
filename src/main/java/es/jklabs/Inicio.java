/*
 * Inicio.java
 *
 * Created on 10 de marzo de 2007, 19:34
 */

package es.jklabs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.jklabs.file.filter.ExeFilter;
import es.jklabs.utilidades.Constantes;
import es.jklabs.utilidades.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author  Juanky
 */
public class Inicio extends javax.swing.JFrame {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final Duration UPDATE_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration UPDATE_REQUEST_TIMEOUT = Duration.ofSeconds(10);
    
    private String rutaArchivo;
    private String rutaSave= "";
    private JMenuItem menuUpdateItem;
    private String updateDownloadUrl;
    
    /** Creates new form Inicio */
    private Inicio() {
        initComponents();
        initUpdateCheck();
    }
    
    private javax.swing.JButton jButton3;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Logger.eliminarLogsVacios();
        Logger.init();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            java.awt.EventQueue.invokeLater(() -> new Inicio().setVisible(true));
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                UnsupportedLookAndFeelException e) {
            Logger.error("interface.load", e);
        }
    }

    private void jButton3ActionPerformed() {//GEN-FIRST:event_jButton3ActionPerformed
        if (!validarRutas()) {
            return;
        }
        jButton3.setEnabled(false);
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                publish(10);
                try {
                    File outputDir = new File(rutaSave);
                    File[] filesBefore = listFiles(outputDir);
                    ProcessBuilder processBuilder = new ProcessBuilder(rutaArchivo, "/x", rutaSave);
                    processBuilder.redirectErrorStream(true);
                    Process process = processBuilder.start();
                    publish(50);
                    CountDownLatch latch = new CountDownLatch(1);
                    SwingUtilities.invokeLater(() -> {
                        Continuar c = new Continuar(Inicio.this, false, latch);
                        c.setLocationRelativeTo(Inicio.this);
                        c.setVisible(true);
                    });
                    latch.await();
                    int exitCode = process.waitFor();
                    if (exitCode != 0) {
                        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                        SwingUtilities.invokeLater(() -> showError("Error al extraer el archivo (codigo " + exitCode + ").\n" + output));
                        return null;
                    }
                    File generatedFile = detectarArchivoGenerado(outputDir, filesBefore);
                    if (generatedFile == null) {
                        SwingUtilities.invokeLater(() -> showError("No se pudo detectar el archivo generado por el instalador."));
                        return null;
                    }
                    File outputFile = resolveOutputFile(outputDir);
                    boolean sameFile = generatedFile.getCanonicalFile().equals(outputFile.getCanonicalFile());
                    if (!sameFile) {
                        Files.move(generatedFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    String outputName = outputFile.getName();
                    SwingUtilities.invokeLater(() -> jTextArea1.setText(jTextArea1.getText() + "-El archivo que contiene lo que usted desea se llama " +
                            outputName + "\n"));
                    publish(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> showError("Imposible abrir el archivo."));
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> showError("Error inesperado al extraer el archivo."));
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int value = chunks.getLast();
                jProgressBar1.setValue(value);
            }

            @Override
            protected void done() {
                jButton3.setEnabled(validarRutasSilencioso());
            }
        };
        worker.execute();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed() {//GEN-FIRST:event_jButton2ActionPerformed
        SelectDir sd= new SelectDir();
        Container parent = this.getParent();
        int choice = sd.jFileChooser1.showOpenDialog(parent);
        if (choice == JFileChooser.APPROVE_OPTION){
            rutaSave= sd.jFileChooser1.getSelectedFile().getAbsolutePath();
            this.jLabel2.setText(rutaSave);
            updateExecuteState();
            return;
        }
        updateExecuteState();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed() {//GEN-FIRST:event_jButton1ActionPerformed
        Open o= new Open();
        Container parent = this.getParent();
        int choice = o.jFileChooser1.showOpenDialog(parent);
        if (choice == JFileChooser.APPROVE_OPTION){
            rutaArchivo= o.jFileChooser1.getSelectedFile().getAbsolutePath();
            this.jLabel1.setText(rutaArchivo);
            updateExecuteState();
            return;
        }
        updateExecuteState();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void updateExecuteState() {
        jButton3.setEnabled(validarRutasSilencioso());
    }

    private boolean validarRutasSilencioso() {
        return validarArchivo(rutaArchivo, false) && validarDirectorio(rutaSave, false);
    }

    private boolean validarRutas() {
        return validarArchivo(rutaArchivo, true) && validarDirectorio(rutaSave, true);
    }

    private boolean validarArchivo(String ruta, boolean mostrarError) {
        if (ruta == null || ruta.isBlank()) {
            if (mostrarError) {
                showError("Selecciona un archivo de origen valido.");
            }
            return false;
        }
        File archivo = new File(ruta);
        if (!archivo.isFile()) {
            if (mostrarError) {
                showError("El archivo de origen no existe.");
            }
            return false;
        }
        return true;
    }

    private boolean validarDirectorio(String ruta, boolean mostrarError) {
        if (ruta == null || ruta.isBlank()) {
            if (mostrarError) {
                showError("Selecciona un directorio de destino valido.");
            }
            return false;
        }
        File dir = new File(ruta);
        if (!dir.isDirectory()) {
            if (mostrarError) {
                showError("El directorio de destino no existe.");
            }
            return false;
        }
        return true;
    }

    private File[] listFiles(File directory) {
        File[] files = directory.listFiles(File::isFile);
        return files != null ? files : new File[0];
    }

    private File detectarArchivoGenerado(File outputDir, File[] filesBefore) {
        File placeholder = new File(outputDir, "%EXENAME%");
        if (placeholder.isFile()) {
            return placeholder;
        }

        Set<String> existingNames = new HashSet<>();
        for (File file : filesBefore) {
            existingNames.add(file.getName());
        }

        File newestNewFile = null;
        for (File file : listFiles(outputDir)) {
            if (!existingNames.contains(file.getName()) &&
                    (newestNewFile == null || file.lastModified() > newestNewFile.lastModified())) {
                newestNewFile = file;
            }
        }
        if (newestNewFile != null) {
            return newestNewFile;
        }

        return Arrays.stream(listFiles(outputDir))
                .max(Comparator.comparingLong(File::lastModified))
                .orElse(null);
    }

    private File resolveOutputFile(File outputDir) {
        String normalizedName = normalizeZipName();
        return resolveOutputFile(outputDir, normalizedName);
    }

    private File resolveOutputFile(File outputDir, String normalizedName) {
        File outputFile = new File(outputDir, normalizedName);
        if (!outputFile.exists()) {
            return outputFile;
        }
        int extensionIndex = normalizedName.toLowerCase().lastIndexOf(".zip");
        String baseName = normalizedName.substring(0, extensionIndex);
        String extension = normalizedName.substring(extensionIndex);
        int index = 1;
        while (outputFile.exists()) {
            outputFile = new File(outputDir, baseName + "-" + index + extension);
            index++;
        }
        return outputFile;
    }

    private String normalizeZipName() {
        if (Constantes.OUTPUT_ZIP_NAME == null || Constantes.OUTPUT_ZIP_NAME.isBlank()) {
            return "Exe.zip";
        }
        String filename = new File(Constantes.OUTPUT_ZIP_NAME.trim()).getName();
        if (filename.isBlank()) {
            return "Exe.zip";
        }
        if (filename.toLowerCase().endsWith(".zip")) {
            return filename;
        }
        return filename + ".zip";
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        // Variables declaration - do not modify//GEN-BEGIN:variables
        JMenuBar menuBar = new JMenuBar();
        JMenu menuAyuda = new JMenu(Constantes.UI_MENU_HELP);
        JMenuItem menuAcercaDe = new JMenuItem(Constantes.UI_MENU_ABOUT);
        menuUpdateItem = new JMenuItem(Constantes.UI_MENU_UPDATE_AVAILABLE);
        JButton jButton1 = new JButton();
        jLabel1 = new javax.swing.JLabel();
        JButton jButton2 = new JButton();
        jLabel2 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        JScrollPane jScrollPane1 = new JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jButton3 = new JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(Constantes.NOMBRE_APP);
        menuAcercaDe.addActionListener(evt -> showInfoDialog());
        menuAyuda.add(menuAcercaDe);
        menuBar.add(menuAyuda);
        menuBar.add(Box.createHorizontalGlue());
        menuUpdateItem.setVisible(false);
        menuUpdateItem.addActionListener(evt -> openUpdateDownload());
        menuUpdateItem.setIcon(loadUpdateIcon());
        menuUpdateItem.setToolTipText(Constantes.UI_MENU_UPDATE_TOOLTIP);
        menuUpdateItem.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        menuUpdateItem.setHorizontalTextPosition(SwingConstants.RIGHT);
        menuBar.add(menuUpdateItem);
        setJMenuBar(menuBar);

        jButton1.setText(Constantes.UI_BUTTON_SOURCE);
        jButton1.addActionListener(evt2 -> jButton1ActionPerformed());

        jLabel1.setText(Constantes.UI_LABEL_SELECT_FILE);

        jButton2.setText(Constantes.UI_BUTTON_DESTINATION);
        jButton2.addActionListener(evt1 -> jButton2ActionPerformed());

        jLabel2.setText(Constantes.UI_LABEL_SELECT_DIRECTORY);

        jProgressBar1.setForeground(new java.awt.Color(0, 0, 204));

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText(Constantes.UI_TEXT_INSTRUCTIONS);
        jScrollPane1.setViewportView(jTextArea1);

        jButton3.setText(Constantes.UI_BUTTON_EXECUTE);
        jButton3.setEnabled(false);
        jButton3.addActionListener(evt -> jButton3ActionPerformed());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1)
                            .addComponent(jButton2))
                        .addGap(14, 14, 14)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)))
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jLabel1))
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jLabel2))
                .addGap(23, 23, 23)
                .addComponent(jButton3)
                .addContainerGap(20, Short.MAX_VALUE))
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void initUpdateCheck() {
        if (Constantes.GITHUB_REPO.isEmpty()) {
            return;
        }
        Thread updateThread = new Thread(this::checkForUpdates, "update-check");
        updateThread.setDaemon(true);
        updateThread.start();
    }

    private void showInfoDialog() {
        JDialog dialog = new JDialog(this, Constantes.UI_DIALOG_INFO_TITLE, true);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(10, 10, 10, 10);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        panel.add(new JLabel("<html><h1>" + Constantes.NOMBRE_APP + " " + Constantes.VERSION + "</h1></html>",
                SwingConstants.CENTER), constraints);

        addAboutAuthor(panel, constraints);
        addPoweredBy(panel, constraints);
        addLicense(panel, constraints);
        addAboutCloseButton(dialog, panel, constraints);

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void addAboutAuthor(JPanel panel, GridBagConstraints constraints) {
        constraints.insets = new Insets(10, 10, 3, 10);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        panel.add(new JLabel(Constantes.UI_INFO_CREATED_BY, SwingConstants.LEFT), constraints);

        constraints.insets = new Insets(3, 10, 3, 10);
        constraints.gridy = 2;
        panel.add(new JLabel("<html><b>" + Constantes.UI_INFO_AUTHOR_NAME + "</b></html>", SwingConstants.LEFT),
                constraints);

        constraints.gridx = 1;
        panel.add(createLinkLabel(Constantes.UI_INFO_WEBSITE_LABEL, Constantes.UI_INFO_WEBSITE_URL), constraints);

        constraints.gridx = 2;
        panel.add(createLinkLabel(Constantes.UI_INFO_EMAIL,
                        "mailto:" + Constantes.UI_INFO_EMAIL + "?subject=" + Constantes.NOMBRE_APP.replace(' ', '_')),
                constraints);
    }

    private void addPoweredBy(JPanel panel, GridBagConstraints constraints) {
        constraints.insets = new Insets(10, 10, 3, 10);
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.gridwidth = 1;
        panel.add(new JLabel(Constantes.UI_INFO_POWERED_BY, SwingConstants.LEFT), constraints);
        addPowered(panel, constraints, 5 + 1, "Jackson", "https://github.com/FasterXML/jackson");
        addPowered(panel, constraints, 5 + 2, "Apache Commons Lang",
                "https://commons.apache.org/proper/commons-lang");
        addPowered(panel, constraints, 5 + 3, "GitHub Releases",
                "https://docs.github.com/repositories/releasing-projects-on-github/about-releases");
        addPowered(panel, constraints, 5 + 4, "Java Swing",
                "https://docs.oracle.com/javase/tutorial/uiswing/");
    }

    private void addPowered(JPanel panel, GridBagConstraints constraints, int y, String title, String url) {
        constraints.insets = new Insets(3, 10, 3, 10);
        constraints.gridx = 0;
        constraints.gridy = y;
        constraints.gridwidth = 1;
        panel.add(createLinkLabel("<html><b>" + title + "</b></html>", url), constraints);

        constraints.gridx = 1;
        constraints.gridwidth = 2;
        panel.add(createLinkLabel(url, url), constraints);
    }

    private void addLicense(JPanel panel, GridBagConstraints constraints) {
        JLabel licenseLabel = createLinkLabel(Constantes.UI_INFO_LICENSE, Constantes.UI_INFO_LICENSE_URL);
        licenseLabel.setIcon(loadIcon());
        licenseLabel.setHorizontalAlignment(SwingConstants.CENTER);
        constraints.insets = new Insets(10, 10, 10, 10);
        constraints.gridx = 0;
        constraints.gridy = 10;
        constraints.gridwidth = 3;
        panel.add(licenseLabel, constraints);
    }

    private void addAboutCloseButton(JDialog dialog, JPanel panel, GridBagConstraints constraints) {
        JButton button = new JButton(Constantes.UI_BUTTON_OK);
        button.addActionListener(evt -> dialog.dispose());
        constraints.gridy = 11;
        constraints.gridwidth = 3;
        panel.add(button, constraints);
    }

    private JLabel createLinkLabel(String text, String uri) {
        JLabel label = new JLabel(text, SwingConstants.LEFT);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                openExternalUri(uri);
            }

            @Override
            public void mouseEntered(MouseEvent event) {
                label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent event) {
                label.setCursor(null);
            }
        });
        return label;
    }

    private void openExternalUri(String uri) {
        try {
            openUri(uri);
        } catch (Exception e) {
            Logger.error("about.open.link", e);
        }
    }

    private void openUri(String uri) throws IOException {
        if (!Desktop.isDesktopSupported()) {
            showError("No se puede abrir el navegador automaticamente en este sistema.");
            return;
        }
        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            showError("El sistema no soporta apertura de enlaces web.");
            return;
        }
        desktop.browse(URI.create(uri));
    }

    private ImageIcon loadIcon() {
        java.net.URL iconUrl = Inicio.class.getClassLoader().getResource("img/icons/gplv3-with-text-136x68.png");
        if (iconUrl == null) {
            return null;
        }
        return new ImageIcon(iconUrl);
    }

    private void checkForUpdates() {
        String apiUrl = "https://api.github.com/repos/" + Constantes.GITHUB_REPO + "/releases/latest";
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(UPDATE_CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(apiUrl))
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", Constantes.NOMBRE_APP)
                    .timeout(UPDATE_REQUEST_TIMEOUT)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return;
            }
            String body = response.body();
            String latestVersion = extractJsonValue(body);
            if (latestVersion == null || !isNewerVersion(latestVersion)) {
                return;
            }
            String downloadUrl = extractAssetUrl(body, latestVersion);
            if (downloadUrl == null) {
                return;
            }
            SwingUtilities.invokeLater(() -> {
                updateDownloadUrl = downloadUrl;
                menuUpdateItem.setText(Constantes.UI_MENU_UPDATE_AVAILABLE + " (" + normalizeVersion(latestVersion) + ")");
                menuUpdateItem.setVisible(true);
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.error("update.check", e);
        } catch (Exception e) {
            Logger.error("update.check", e);
        }
    }

    private ImageIcon loadUpdateIcon() {
        java.net.URL iconUrl = Inicio.class.getClassLoader().getResource("img/icons/update.png");
        if (iconUrl == null) {
            return null;
        }
        return new ImageIcon(iconUrl);
    }

    private String extractJsonValue(String json) {
        JsonNode release = readJson(json);
        if (release == null) {
            return null;
        }
        JsonNode tagName = release.get("tag_name");
        if (tagName == null || tagName.isNull()) {
            return null;
        }
        return tagName.asText();
    }

    private static String getZipUrl(JsonNode assets) {
        for (JsonNode asset : assets) {
            JsonNode browserUrl = asset.get("browser_download_url");
            if (browserUrl != null && !browserUrl.isNull()) {
                String url = browserUrl.asText();
                if (url.toLowerCase().endsWith(".zip")) {
                    return url;
                }
            }
        }
        return null;
    }

    private String extractAssetUrl(String json, String latestVersion) {
        JsonNode release = readJson(json);
        JsonNode assets = getReleaseAssets(release);
        if (assets == null) {
            return null;
        }
        String namedAssetUrl = getNamedAssetUrl(assets, buildAssetName(latestVersion));
        if (namedAssetUrl != null) {
            return namedAssetUrl;
        }
        return getZipUrl(assets);
    }

    private JsonNode getReleaseAssets(JsonNode release) {
        if (release == null) {
            return null;
        }
        JsonNode assets = release.get("assets");
        if (assets == null || !assets.isArray()) {
            return null;
        }
        return assets;
    }

    private String getNamedAssetUrl(JsonNode assets, String desiredAssetName) {
        if (desiredAssetName == null || desiredAssetName.isBlank()) {
            return null;
        }
        for (JsonNode asset : assets) {
            if (isNamedAsset(asset, desiredAssetName)) {
                String browserUrl = getBrowserDownloadUrl(asset);
                if (browserUrl != null) {
                    return browserUrl;
                }
            }
        }
        return null;
    }

    private boolean isNamedAsset(JsonNode asset, String desiredAssetName) {
        JsonNode assetName = asset.get("name");
        return assetName != null && desiredAssetName.equals(assetName.asText());
    }

    private String getBrowserDownloadUrl(JsonNode asset) {
        JsonNode browserUrl = asset.get("browser_download_url");
        if (browserUrl == null || browserUrl.isNull()) {
            return null;
        }
        return browserUrl.asText();
    }

    private void openUpdateDownload() {
        if (updateDownloadUrl == null || updateDownloadUrl.isEmpty()) {
            return;
        }
        try {
            openUri(updateDownloadUrl);
        } catch (Exception e) {
            Logger.error("update.open.download", e);
        }
    }

    private String buildAssetName(String latestVersion) {
        if (Constantes.GITHUB_ASSET_PATTERN == null || Constantes.GITHUB_ASSET_PATTERN.isEmpty()) {
            return null;
        }
        String normalizedVersion = normalizeVersion(latestVersion);
        return Constantes.GITHUB_ASSET_PATTERN.replace("{version}", normalizedVersion);
    }

    private boolean isNewerVersion(String latestVersion) {
        int comparison = compareVersionParts(normalizeVersion(Constantes.VERSION), normalizeVersion(latestVersion));
        return comparison < 0;
    }

    private String normalizeVersion(String version) {
        if (version == null) {
            return "";
        }
        String normalized = version.trim();
        if (normalized.startsWith("v") || normalized.startsWith("V")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private int compareVersionParts(String currentVersion, String latestVersion) {
        String[] currentParts = currentVersion.split("\\.");
        String[] latestParts = latestVersion.split("\\.");
        int max = Math.max(currentParts.length, latestParts.length);
        for (int i = 0; i < max; i++) {
            int current = i < currentParts.length ? parseVersionPart(currentParts[i]) : 0;
            int latest = i < latestParts.length ? parseVersionPart(latestParts[i]) : 0;
            if (current != latest) {
                return Integer.compare(current, latest);
            }
        }
        return currentVersion.compareTo(latestVersion);
    }

    private int parseVersionPart(String part) {
        Matcher matcher = Pattern.compile("^(\\d+)").matcher(part);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }
    
    private JsonNode readJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return JSON_MAPPER.readTree(json);
        } catch (IOException e) {
            Logger.error("update.read.response", e);
            return null;
        }
    }

    public static class Open extends javax.swing.JFrame {
        
    private javax.swing.JFileChooser jFileChooser1;
    
    /** Creates new form Open */
    Open() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">                          
    private void initComponents() {
        jFileChooser1 = new javax.swing.JFileChooser();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            jFileChooser1.setFileFilter(new ExeFilter());

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jFileChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jFileChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
            );
            pack();
        }
    
    }

    public static class SelectDir extends javax.swing.JFrame {
    
    /** Creates new form SelectDir */
    SelectDir() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">                          
    private void initComponents() {
        jFileChooser1 = new javax.swing.JFileChooser();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        jFileChooser1.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        jFileChooser1.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jFileChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jFileChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
    }// </editor-fold>                        
    
    
    // Variables declaration - do not modify                     
    private javax.swing.JFileChooser jFileChooser1;
    // End of variables declaration                   
    
}

    private void showError(String message) {
        JOptionPane.showMessageDialog(new Frame(), message, Constantes.UI_DIALOG_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
    }

    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;

    public static class Continuar extends javax.swing.JDialog {
        private final transient CountDownLatch latch;

    /** Creates new form Continuar */
    Continuar(java.awt.Frame parent, boolean modal, CountDownLatch latch) {
        super(parent, modal);
        this.latch = latch;
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">
    private void initComponents() {
        JLabel jLabel = new JLabel();
        // Variables declaration - do not modify
        JButton jButton1 = new JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        jLabel.setText(Constantes.UI_DIALOG_CONTINUE_MESSAGE);

        jButton1.setText(Constantes.UI_BUTTON_CONTINUE);
        jButton1.addActionListener(evt -> jButton1ActionPerformed());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel)
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addContainerGap())
        );
        pack();
    }// </editor-fold>

    private void jButton1ActionPerformed() {
        latch.countDown();
        dispose();
    }

     // End of variables declaration


}
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
    
}
