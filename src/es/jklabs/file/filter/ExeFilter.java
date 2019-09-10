/*
 * ExeFilter.java
 *
 * Created on 11 de marzo de 2007, 11:53
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package es.jklabs.file.filter;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Juanky
 */
public class ExeFilter extends FileFilter {

    /** Creates a new instance of ExeFilter */
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
            String exe = "exe";
            return exe.equals(extension);
        }

        return false;
    }

    public String getDescription() {
        return "*.exe, *.msi";
    }
    
}
