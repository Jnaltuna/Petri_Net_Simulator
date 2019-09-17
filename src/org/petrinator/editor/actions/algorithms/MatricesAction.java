/*
 * Copyright (C) 2008-2010 Martin Riesz <riesz.martin at gmail.com>
 * Copyright (C) 2016-2017 Joaquin Rodriguez Felici <joaquinfelici at gmail.com>
 * Copyright (C) 2016-2017 Leandro Asson <leoasson at gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.petrinator.editor.actions.algorithms;

import org.petrinator.editor.Root;

import org.petrinator.petrinet.Document;
import org.petrinator.petrinet.Transition;
import org.petrinator.petrinet.Marking;
import org.petrinator.util.GraphicsTools;
import pipe.gui.widgets.ButtonBar;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.ResultsHTMLPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import java.awt.*;
import java.util.*;


public class MatricesAction extends AbstractAction
{
    private Root root;
    private ResultsHTMLPane results;

    public MatricesAction(Root root) {
        this.root = root;

        String name = "Matrices";
        putValue(NAME, name);
        putValue(SHORT_DESCRIPTION, name);
        putValue(SMALL_ICON, GraphicsTools.getIcon("pneditor/matrices16.png"));
    }

    public void actionPerformed(ActionEvent e) {
        /*
         * Show initial pane
         */
        EscapableDialog guiDialog = new EscapableDialog(root.getParentFrame(), "Petri net matrices and marking", true);
        Container contentPane = guiDialog.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        results = new ResultsHTMLPane("");
        contentPane.add(results);
        contentPane.add(new ButtonBar("Calculate", calculateButtonClick, guiDialog.getRootPane()));
        guiDialog.pack();
        guiDialog.setLocationRelativeTo(root.getParentFrame());
        guiDialog.setVisible(true);
    }

    private final ActionListener calculateButtonClick = new ActionListener() {

        public void actionPerformed(ActionEvent arg0)
        {

            if(!root.getDocument().getPetriNet().getRootSubnet().isValid()) {
                JOptionPane.showMessageDialog(null, "Invalid Net!", "Error", JOptionPane.ERROR_MESSAGE, null);
                return;
            }
            else
            {
                /* Create HTML file with data */
                String s = "<h2>Petri Net Matrices</h2>";

                ArrayList<String> pnames = root.getDocument().getPetriNet().getSortedPlacesNames();
                ArrayList<String> tnames = root.getDocument().getPetriNet().getSortedTransitionsNames();

                try
                {
                    s += ResultsHTMLPane.makeTable(new String[]{
                            "Forwards incidence matrix <i>I<sup>+</sup></i>",
                            renderMatrix(pnames,tnames,root.getDocument().getPetriNet().forwardIMatrix())
                    }, 1, false, false, true, false);
                    s += ResultsHTMLPane.makeTable(new String[]{
                            "Backwards incidence matrix <i>I<sup>-</sup></i>",
                            renderMatrix(pnames,tnames,root.getDocument().getPetriNet().backwardsIMatrix())
                    }, 1, false, false, true, false);
                    s += ResultsHTMLPane.makeTable(new String[]{
                            "Combined incidence matrix <i>I</i>",
                            renderMatrix(pnames,tnames,root.getDocument().getPetriNet().incidenceMatrix())
                    }, 1, false, false, true, false);
                    s += ResultsHTMLPane.makeTable(new String[]{
                            "Inhibition matrix <i>H</i>",
                            renderMatrix(pnames,tnames,root.getDocument().getPetriNet().inhibitionMatrix())
                    }, 1, false, false, true, false);
                    s += ResultsHTMLPane.makeTable(new String[]{
                            "Reset matrix <i>H</i>",
                            renderMatrix(pnames,tnames,root.getDocument().getPetriNet().resetMatrix())
                    }, 1, false, false, true, false);
                    s += ResultsHTMLPane.makeTable(new String[]{
                            "Reader matrix <i>H</i>",
                            renderMatrix(pnames,tnames,root.getDocument().getPetriNet().readerMatrix())
                    }, 1, false, false, true, false);
                    s += ResultsHTMLPane.makeTable(new String[]{
                            "Marking",
                            renderMarkingMatrices(pnames, root.getDocument())
                    }, 1, false, false, true, false);
                    s += ResultsHTMLPane.makeTable(new String[]{
                            "Enabled transitions",
                            renderTransitionStates(tnames, root.getDocument())
                    }, 1, false, false, true, false);
                }
                catch(OutOfMemoryError oome)
                {
                    System.gc();
                    results.setText("");
                    s = "Memory error: " + oome.getMessage();

                    s += "<br>Not enough memory. Please use a larger heap size." + "<br>" + "<br>Note:" + "<br>The Java heap size can be specified with the -Xmx option." + "<br>E.g., to use 512MB as heap size, the command line looks like this:" + "<br>java -Xmx512m -classpath ...\n";
                    results.setText(s);
                    return;
                }
                catch(Exception e)
                {
                    //e.printStackTrace();
                    s = "<br>Invalid net";
                    results.setText(s);
                    return;
                }

                results.setEnabled(true);
                results.setText(s);

            }
        }
    };

    /**
     * Format a matrix as HTML
     *
     * @param pnames arraylist with the net's places names
     * @param tnames arraylist with the net's transitions names
     */
    private String renderMatrix(ArrayList<String> pnames, ArrayList<String> tnames, int[][] matrix) {

        if((matrix.length == 0) || (matrix[0].length == 0))
        {
            return "n/a";
        }

        ArrayList result = new ArrayList();
        result.add("");
        for(int i = 0; i < matrix[0].length; i++)
        {
            result.add(tnames.get(i));
        }

        for(int i = 0; i < matrix.length; i++)
        {
            result.add(pnames.get(i));
            for(int j = 0; j < matrix[i].length; j++)
            {
                result.add(Integer.toString(matrix[i][j]));
            }
        }

        return ResultsHTMLPane.makeTable(
                result.toArray(), matrix[0].length + 1, false, true, true, true);


    }

    /**
     * Format an arraylist as HTML
     *
     * @param pnames arraylist with the net's places names
     * @param doc current Document
     */
    private String renderMarkingMatrices(ArrayList<String> pnames, Document doc) {

        Marking mark = doc.getPetriNet().getInitialMarking();
        int markingMatrix [][] =  mark.getMarkingAsArray();
        if(markingMatrix == null)
        {
            return "n/a";
        }

        ArrayList result = new ArrayList();
        // add headers t o table

        result.add("");
        for(String name : pnames)
        {
            result.add(name);
        }

        result.add("Initial");

        for(int i = 0; i< markingMatrix[Marking.INITIAL].length; i++){
            result.add(Integer.toString(markingMatrix[Marking.INITIAL][i]));
        }
        result.add("Current");
        for(int j = 0; j< markingMatrix[Marking.CURRENT].length; j++){
            result.add(Integer.toString(markingMatrix[Marking.CURRENT][j]));
        }

        return ResultsHTMLPane.makeTable(
                result.toArray(), pnames.size() + 1, false, true, true, true);
    }

    /**
     * Format transitions states as HTML
     *
     * @param sortedNames array list with the transitions labels
     * @param doc current Document
     */
    private String renderTransitionStates(ArrayList<String> sortedNames, Document doc) {

        ArrayList<Transition> enabledArray = new ArrayList<Transition>(doc.getPetriNet().getInitialMarking().getAllEnabledTransitions());
        ArrayList<String> enabledNamesArray = new ArrayList<>();

        for (Transition transition : enabledArray) {
            enabledNamesArray.add(transition.getLabel());
        }

        if(sortedNames.size() == 0)
        {
            return "n/a";
        }

        ArrayList result = new ArrayList();


        result.add("");
        for(int i=0; i<sortedNames.size(); i++){
            result.add(sortedNames.get(i));
        }
        result.add("Enabled");
        for(int i=0; i<sortedNames.size(); i++){

            if(enabledNamesArray.contains(sortedNames.get(i))){
                result.add("yes");
            }
            else{
                result.add("no");
            }

        }

        return ResultsHTMLPane.makeTable(
                result.toArray(), sortedNames.size() + 1, false, false, true, true);
    }
}
