/*
 * ExeFilter.java
 *
 * Created on 11 de marzo de 2007, 11:53
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package es.jklabs.file.filter;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 *
 * @author Juanky
 */
public class ExeFilter extends FileFilter {

    /**
     * Constructs a new instance of the ExeFilter class.
     * <p>
     * This file filter implementation is designed to work with file dialogs
     * or file selection mechanisms, filtering and allowing only files
     * with extensions ".exe" and ".msi", as well as directories.
     * It identifies the file extensions in a case-insensitive manner.
     */
    public ExeFilter() {
    }

    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }
        String s = file.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 &&  i < s.length() - 1) {
            String extension = s.substring(i+1).toLowerCase();
            return "exe".equals(extension) || "msi".equals(extension);
        }

        return false;
    }

    public String getDescription() {
        return "*.exe, *.msi";
    }
    
}
