package org.petrinator.editor.actions.algorithms.newReachability;

import java.util.ArrayList;
import java.util.Arrays;

public class TreeNode {

    private TreeNode parent;
    private ArrayList<TreeNode> children;
    private int[] marking;
    private boolean[] enabledTransitions;
    private CRTree tree;

    private int id;
    private int depth;

    private ArrayList<Integer> pathToDeadlock;
    private boolean deadlock;

    private int fromTransition;


    TreeNode(CRTree tree, int[] marking, int fromTransition, TreeNode parent, int depth) {

        this.marking = marking;
        this.parent = parent;
        this.depth = depth;
        this.tree = tree;
        this.fromTransition = fromTransition;
        children = new ArrayList<>();

        enabledTransitions = tree.areTransitionsEnabled(this.marking);
        deadlock = true;

    }

    private String getNodeId() {
        return String.format("S%-4d", id);
    }

    void recursiveExpansion() {

        boolean allOmegas;
        boolean repeated;

        for (int i = 0; i < enabledTransitions.length; i++) {

            if (enabledTransitions[i]) {

                deadlock = false;

                children.add(new TreeNode(tree, tree.fire(i, marking), i + 1, this, depth + 1));

                allOmegas = children.get(children.size()-1).insertOmegas();

                int[] rs = tree.repeatedState(children.get(children.size() - 1).marking).clone(); //todo ver si esta funcionando bien id
                repeated = (rs[CRTree.REPEATED] == 1);
                children.get(children.size() - 1).id = rs[CRTree.STATE];

                //repeated = children.get(children.size()-1).repeatedState;

                if (!repeated /*&& !allOmegas*/) {
                    children.get(children.size() - 1).recursiveExpansion();
                }
                //size -1 me devuelve el children de esta iteracion
                //ver si me hace falta mantener el orden
            }
        }

        if (deadlock) {
            System.out.println("Hay deadlock");
            recordDeadPath();
            tree.setDeadLock(pathToDeadlock);
        }
    }
    /**
     * Fills the given matrix with the reachability information
     * rows are source states, columns are destination states
     * and the value in any M[i][j] will be the transition required
     * to reach state j from state i if possible
     */
    void recursiveMatrix(int[][] reachabilityMatrix){

        int childrenCount = children.size();
        if(childrenCount > 0){
            for(int i=0; i<childrenCount; i++){
                children.get(i).recursiveMatrix(reachabilityMatrix);
            }

            for(int i=0; i<childrenCount; i++){
                reachabilityMatrix[id][children.get(i).id] = children.get(i).fromTransition;
            }
        }

    }

    private void recordDeadPath() {

        pathToDeadlock = new ArrayList<>();
        pathToDeadlock.add(fromTransition);

        ArrayList<TreeNode> nodePath = new ArrayList<>();
        nodePath.add(this);

        for (int i = 0; i < depth; i++) {
            nodePath.add(nodePath.get(i).parent);
            pathToDeadlock.add(nodePath.get(i+1).fromTransition);
        }

    }

    /**
     *
     * Checks if any omegas need to be inserted in the places of a given node.
     * Omegas (shown by -1 here) represent unbounded places and are therefore
     * important when testing whether a petri net is bounded. This function
     * checks each of the ancestors of a given node.
     *
     * @return true if all places now contain an omega.
     */
    private boolean insertOmegas() {

        //Attributes used for assessing boundedness of the net
        boolean allElementsGreaterOrEqual;
        boolean insertedOmega = false;
        TreeNode ancestorNode;

        boolean[] elementIsStrictlyGreater = new boolean[tree.getPlaceCount()];

        //Initialize array to false
        Arrays.fill(elementIsStrictlyGreater, false);

        ancestorNode = this;

        //For each ancestor node until root
        while (ancestorNode != tree.getRoot() && !insertedOmega) {
            //Take parent of current ancestor
            ancestorNode = ancestorNode.parent;

            allElementsGreaterOrEqual = true;

            //compare marking of this node to the current ancestor reference
            //if any place has a lower marking, set allElementsGreaterOrEqual to false
            for (int i = 0; i < tree.getPlaceCount(); i++) {

                if (marking[i] != -1) {

                    if (marking[i] < ancestorNode.marking[i]) {
                        allElementsGreaterOrEqual = false;
                        break;
                    }

                    elementIsStrictlyGreater[i] = (marking[i] > ancestorNode.marking[i]);

                }
            }

            //Assess the information obtained for this node
            if (allElementsGreaterOrEqual) {

                for(int p = 0; p<tree.getPlaceCount(); p++){
                    //check inhibition for each place
                    boolean inhibition = false;
                    for (int t = 0; t < tree.getTransitionCount(); t++) {
                        //check if there is an inhibiton arc asociated to this place
                        int inhibiton_value = tree.getInhibition()[p][t];
                        //check if the place inhibits the transition
                        if (inhibiton_value > 0 && (marking[p] <= inhibiton_value)) {
                            inhibition = true;
                            break;
                        }
                    }

                    if (!inhibition) {
                        if (marking[p] != -1 && elementIsStrictlyGreater[p]) {
                            marking[p] = -1;
                            insertedOmega = true;
                            tree.setFoundAnOmega();
                        }
                    }
                }

            }
        }

        for (int i = 0; i < tree.getPlaceCount(); i++) {
            if (marking[i] != -1) {
                return false;
            }
        }

        return true;
    }

    public int[] getMarking() {
        return marking;
    }

}
