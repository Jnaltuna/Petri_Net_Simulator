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

import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;
import org.petrinator.editor.Root;
import org.petrinator.petrinet.Marking;
import org.petrinator.util.GraphicsTools;
import pipe.exceptions.TreeTooBigException;
import pipe.gui.widgets.ButtonBar;
import pipe.gui.widgets.ResultsHTMLPane;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import org.petrinator.editor.actions.algorithms.newReachability.CRTree;

import org.graphstream.graph.*;


/**
 * Generates the reachability/coverability graph representation for the Petri Net
 */
public class ReachabilityAction extends AbstractAction
{
    private Root root;
    private ResultsHTMLPane results;
    private JDialog guiDialog;

    public ReachabilityAction(Root root)
    {
        this.root = root;
        String name = "Reachabilty/Coverability graph";
        putValue(NAME, name);
        putValue(SHORT_DESCRIPTION, name);
        putValue(SMALL_ICON, GraphicsTools.getIcon("pneditor/graph16.png"));

        guiDialog = new JDialog(root.getParentFrame(), "Reachabilty/Coverability graph", false);

        guiDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        /*
            Sets variables on null after closing the dialog window
            to free memory
         */
        guiDialog.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e)
            {
                results = null;
            }
        });

        Container contentPane = guiDialog.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        results = new ResultsHTMLPane("");
        contentPane.add(results);
        contentPane.add(new ButtonBar("Generate graph", new GenerateListener(), guiDialog.getRootPane()));

    }


    /**
     * Resets and shows the 'Reachability/Coverability' initial dialog window
     */
    public void actionPerformed(ActionEvent e)
    {
        results.setText("");

        // Disables the copy and save buttons
        results.setEnabled(false);

        guiDialog.pack();
        guiDialog.setLocationRelativeTo(root.getParentFrame());
        guiDialog.setVisible(true);


    }


    /**
     * Generate Button Listener
     */
    private class GenerateListener implements ActionListener {

        public void actionPerformed(ActionEvent actionEvent) {

            // Checks if the net is valid
            if(!root.getDocument().getPetriNet().getRootSubnet().isValid()) {
                JOptionPane.showMessageDialog(null, "Invalid Net!", "Error", JOptionPane.ERROR_MESSAGE, null);
                return;
            }

            String log = "<h2>Reachability/Coverability Graph Information</h2>";


            //TODO check tree size
            CRTree arbol = new CRTree(root, root.getCurrentMarking().getMarkingAsArray()[Marking.CURRENT]);
            log += arbol.getTreeLog();

            generateGraph(arbol.getReachabilityMatrix());

            results.setText(log);

            // Enables the copy and save buttons
            results.setEnabled(true);

        }
    };

    /**
     * Displays graph using Graphstream library
     * @param stateMatrix matrix that contains all the states and possible transitions
     */
    private void generateGraph(int[][] stateMatrix){
        Graph graph = new SingleGraph("Reachability/Coverability");

        //Create a node for each state
        //Each state has a label indicated by S + state number
        for(int i = 0;i<stateMatrix.length; i++){
            String s = Integer.toString(i);
            Node n = graph.addNode(s);
            n.addAttribute("ui.label", "S"+s);
        }

        //Create arrows that join the previous states based on the values of the stateMatrix
        //Each arrow has a label based on the transition fired that caused the change in state
        for(int i=0;i<stateMatrix.length; i++){
            for(int j = 0; j < stateMatrix[0].length; j++){
                if(stateMatrix[i][j] != -1){
                    String ename = "S" + Integer.toString(i) + "-" + Integer.toString(j);
                    Edge e = graph.addEdge(ename,Integer.toString(i),Integer.toString(j),true);
                    e.addAttribute("ui.label","T" + Integer.toString(stateMatrix[i][j]));

                }
            }
        }

        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        Viewer viewer = graph.display();
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.CLOSE_VIEWER);

        //Atributes for all nodes
        graph.addAttribute("ui.stylesheet","node {\n" +
                "\tsize: 30px;\n" +
                "\tshape: circle;\n" +
                "\tstroke-mode: plain;\n" +
                "\tstroke-color: black;\n" +
                "\tstroke-width: 1;\n" +
                "\ttext-mode: normal;\n" +
                "\ttext-style: bold;\n" +
                "\tfill-color: rgb(156,230,255);\n" +
                "\tz-index: 1;\n" +
                "}");
        //Atributes for all edges
        graph.addAttribute("ui.stylesheet","edge {\n" +
                "\ttext-mode: normal;\n" +
                "\ttext-style: bold;\n" +
                "\ttext-alignment: center;\n" +
                "\tz-index: 0;  \n" +
                "}");

    }

}
