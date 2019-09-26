package org.petrinator.editor.actions.algorithms.newReachability;

import java.util.ArrayList;
import java.util.Arrays;

public class TreeNode {

    static int nodes = 0;
    private int id;

    private TreeNode parent;
    private ArrayList<TreeNode> children;
    private int[] marking;
    private boolean[] enabledTransitions;
    private CRTree tree;

    private int depth;

    private ArrayList<int[]> pathToDeadlock;
    private boolean deadlock;
    private boolean repeatedState;

    private int fromTransition;


    public TreeNode(CRTree tree, int[] marking, int fromTransition, TreeNode parent, int depth){

        this.marking = marking;
        this.parent = parent;
        this.depth = depth;
        this.tree = tree;
        this.fromTransition = fromTransition;
        children = new ArrayList<>();

        id = nodes;
        nodes++;

        enabledTransitions = tree.areTransitionsEnabled(this.marking);
        deadlock = true;

        int[] rs = tree.repeatedState(this.marking).clone();

        repeatedState = (rs[CRTree.REPEATED] == 1);
        id = rs[CRTree.STATE];

    }

    String getNodeId(){
        return "E"+id;
    }

    void recursiveExpansion(){

        boolean allOmegas;

        //TODO si se quiere saber todos los caminos preguntar adentro del for
        if(repeatedState){
            return;
        }

        for(int i=0; i<enabledTransitions.length; i++){

            if(enabledTransitions[i]){

                deadlock = false;

                children.add(new TreeNode(tree, tree.fire(i, marking), i+1, this, depth+1));

                //TODO add omegas, verify if its repeated
                allOmegas = children.get(children.size()-1).InsertOmegas();

                repeatedNode = children.get(children.size()-1).repeatedNode;

                if(!repeatedNode && !allOmegas) {
                    children.get(children.size() - 1).recursiveExpansion();
                }
                //size -1 me devuelve el children de esta iteracion
                //ver si me hace falta mantener el orden
            }
        }

        if(deadlock){
            System.out.println("Hay deadlock");
            recordDeadPath();
            tree.setDeadLock(pathToDeadlock);
            /*for(int i=pathToDeadlock.size()-1; i>=0; i--){
                System.out.println(Arrays.toString(pathToDeadlock.get(i)));
            }*/
        }
    }

    void recursiveLog(){

        int childrenCount = children.size();

        if(childrenCount > 0){
            for(int i=0; i<childrenCount; i++){
                children.get(i).recursiveLog();
            }
            String from = String.format("\n-- Reachable states from %3s %s:\n\n", getNodeId(), Arrays.toString(marking));
            System.out.println(from);
            for (int j=0; j<childrenCount; j++){
                String state = String.format("     T%d -> * %s - %s\n", children.get(j).fromTransition ,children.get(j).getNodeId(), Arrays.toString(children.get(j).marking));
                System.out.println(state);
            }

        }

    }

    private void recordDeadPath(){

        pathToDeadlock = new ArrayList<>();
        pathToDeadlock.add(marking);

        ArrayList<TreeNode> nodePath = new ArrayList<>();
        nodePath.add(this);

        for(int i=0; i<depth; i++){
            nodePath.add(nodePath.get(i).parent);
            pathToDeadlock.add(nodePath.get(i+1).getMarking());
        }

    }

    /**
     * Function: void InsertOmegas()
     * Checks if any omegas need to be inserted in the places of a given node.
     * Omegas (shown by -1 here) represent unbounded places and are therefore
     * important when testing whether a petri net is bounded. This function
     * checks each of the ancestors of a given node.
     * Returns true iff all places now contain an omega.
     * @return
     */
    public boolean InsertOmegas(){
        //Attributes used for assessing boundedness of the net
        boolean allElementsGreaterOrEqual;
        boolean insertedOmega = false;
        TreeNode ancestorNode;

        boolean [] elementIsStrictlyGreater = new boolean[tree.getPlaceCount()];

        //Initialize array to false
        Arrays.fill(elementIsStrictlyGreater,false);

        ancestorNode = this;

        //For each ancestor node until root
        while (ancestorNode != tree.getRoot() && !insertedOmega){
            //Take parent of current ancestor
            ancestorNode = ancestorNode.parent;

            allElementsGreaterOrEqual = true;

            //compare marking of this node to the current ancestor reference
            //if any place has a lower marking, set allElementsGreaterOrEqual to false
            for(int i = 0; i < tree.getPlaceCount(); i++){

                if(marking[i] != -1)
                {

                    if(marking[i] < ancestorNode.marking[i]){
                        allElementsGreaterOrEqual = false;
                        break;
                    }

                    elementIsStrictlyGreater[i] = (marking[i] > ancestorNode.marking[i]);

                }
            }

            //Assess the information obtained for this node
            if(allElementsGreaterOrEqual) {

                for(int p = 0; p< tree.getPlaceCount(); p++){
                    //check inhibition for each place
                    boolean inhibition = false;
                    for(int t = 0; t< tree.getTransitionCount(); t++){
                        //check if there is an inhibiton arc asociated to this place
                        int inhibiton_value = tree.getInhibition()[p][t];
                        if(inhibiton_value > 0 && (marking[p] <= inhibiton_value)){
                            inhibition = true;
                            break;
                        }
                    }

                    if(!inhibition){
                        if(marking[p] != -1 && elementIsStrictlyGreater[p]){
                            marking[p] = -1;
                            insertedOmega = true;
                            tree.setFoundAnOmega();
                        }
                    }
                }
            }
        }

        for(int i = 0; i< tree.getPlaceCount(); i++){
            if(marking[i] != -1){
                return false;
            }
        }

        return true;
    }

    public int[] getMarking() {
        return marking;
    }

}
