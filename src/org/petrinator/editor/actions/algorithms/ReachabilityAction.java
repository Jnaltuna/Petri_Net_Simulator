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
import org.petrinator.petrinet.Marking;
import org.petrinator.util.GraphicsTools;
import pipe.exceptions.TreeTooBigException;
import pipe.gui.widgets.ButtonBar;
import pipe.gui.widgets.ResultsHTMLPane;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;

import org.petrinator.editor.actions.algorithms.newReachability.CRTree;


/**
 * Generates the reachability/coverability graph representation for the Petri Net
 */
public class ReachabilityAction extends AbstractAction
{
    private Root root;
    //private String graphName = "";
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

            /*long start = new Date().getTime();
            long gfinished;
            long allfinished;
            double graphtime;
            double constructiontime;
            double totaltime;*/

            /*
             * Let's try to create the reachability graph
             */
            File reachabilityGraph = new File("results.rg");
            /*if(!root.getDocument().getPetriNet().getRootSubnet().hasPlaces() || !root.getDocument().getPetriNet().getRootSubnet().hasTransitions())
            {
                s += "Invalid net!";
            }
            else
            {
                try
                {
                    /*
                     * Check if petri net is bounded

                    LinkedList<MarkingView>[] markings = sourcePetriNetView.getCurrentMarkingVector();
                    int[] markup = new int[markings.length];
                    for(int k = 0; k < markings.length; k++)
                    {
                        markup[k] = markings[k].getFirst().getCurrentMarking();
                    }
                    CRTree tree = new CRTree(root, markup);
                    boolean bounded = !tree.isFoundAnOmega();

                    if(bounded)
                    {
                        StateSpaceGenerator.generate(sourcePetriNetView, reachabilityGraph);
                        graphName = "Reachability graph";
                        System.out.println("Reachability graph successfully created");
                    }
                    else
                    {
                         /*
                          * If we found the net to be unbounded, then we need to create the coverability graph

                        LinkedList<MarkingView>[] graphMarkings = sourcePetriNetView.getCurrentMarkingVector();
                        int[] currentMarking = new int[markings.length];
                        for(int i = 0; i < markings.length; i++)
                        {
                            currentMarking[i] = markings[i].getFirst().getCurrentMarking();
                        }
                        CRTree graphTree = new CRTree(root, currentMarking, reachabilityGraph);
                        graphName = "Coverability graph";
                        System.out.println("Coverability graph successfully created");
                    }

                    /*
                     * Let's show the results

                    gfinished = new Date().getTime();
                    System.gc();
                    generateGraph(reachabilityGraph, sourcePetriNetView, !bounded);
                    allfinished = new Date().getTime();
                    graphtime = (gfinished - start) / 1000.0;
                    constructiontime = (allfinished - gfinished) / 1000.0;
                    totaltime = (allfinished - start) / 1000.0;
                    DecimalFormat f = new DecimalFormat();
                    f.setMaximumFractionDigits(5);
                    s += "<br>Generating " + graphName + " took " +
                            f.format(graphtime) + "s";
                    s += "<br>Constructing it took " +
                            f.format(constructiontime) + "s";
                    s += "<br>Total time was " + f.format(totaltime) + "s";
                    results.setEnabled(true);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                results.setText(s);
            }*/


            try {
                CRTree arbol = new CRTree(root, root.getCurrentMarking().getMarkingAsArray()[Marking.CURRENT]);
                log += arbol.getTreeLog();
            } catch (TreeTooBigException e) {
                e.printStackTrace();
            }

            results.setText(log);

            // Enables the copy and save buttons
            results.setEnabled(true);
        }


    };

    /*public void generateGraph(File rgFile, PetriNetView dataLayer, boolean coverabilityGraph) throws Exception
    {
        ReachabilityGraphGenerator graphGenerator = new ReachabilityGraphGenerator();

        DefaultGraph graph = graphGenerator.createGraph(rgFile, dataLayer, coverabilityGraph);

        GraphFrame frame = new GraphFrame();
        PlaceView[] placeView = dataLayer.places();
        String legend = "";
        if (placeView.length > 0) {
            legend = "{" + placeView[0].getName();
        }
        for (int i = 1; i < placeView.length; i++) {
            legend += ", " + placeView[i].getName();
        }
        legend += "}";
        frame.setTitle(graphName);
        frame.constructGraphFrame(graph, legend, root);
        frame.toFront();
    }*/
}
