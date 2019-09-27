package org.petrinator.editor.actions.algorithms.newReachability;

import org.petrinator.editor.Root;

import java.util.ArrayList;
import java.util.Arrays;

public class CRTree {

    static final int REPEATED = 0;
    static final int STATE = 1;
    private static final int NAN = -1;

    private boolean bounded = true;
    private boolean moreThanOneToken = false;

    private boolean deadlock = false;
    private ArrayList<Integer> SPDeadlock;

    private ArrayList<int[]> statesList;

    private TreeNode rootNode;

    private int [][] iMinus;
    private int [][] iCombined;
    private int [][] inhibition;
    private int [][] reset;
    private int [][] reader;

    private boolean hasInhibitionArcs;
    private boolean hasResetArcs;
    private boolean hasReaderArcs;

    private final int transitionCount;
    private final int placeCount;

    private int[][] reachMatrix;

    public CRTree(Root root, int[] initialMarking) {

        //this.moreThanOneToken = isSafe(treeRoot);

        iMinus = root.getDocument().getPetriNet().backwardsIMatrix();
        iCombined = root.getDocument().getPetriNet().incidenceMatrix();
        inhibition = root.getDocument().getPetriNet().inhibitionMatrix();
        reset = root.getDocument().getPetriNet().resetMatrix();
        reader = root.getDocument().getPetriNet().readerMatrix();

        hasInhibitionArcs = isMatrixNonZero(inhibition);
        hasReaderArcs = isMatrixNonZero(reader);
        hasResetArcs = isMatrixNonZero(reset);

        transitionCount = iMinus[0].length;
        placeCount = iMinus.length;

        statesList = new ArrayList<>();

        rootNode = new TreeNode(this, initialMarking, -1, rootNode, 0);
        statesList.add(initialMarking); //add initial marking to state list

        rootNode.recursiveExpansion(); //generates the tree

        reachMatrix = new int[statesList.size()][statesList.size()];

        for (int[] matrix : reachMatrix) {
            Arrays.fill(matrix, NAN);
        }

        rootNode.recursiveMatrix(reachMatrix); //generates reachability matrix
    }

    /**
     * Generates a string with the reachability/coverability information
     * of the net using the reachability matrix
     * @return log string with html format
     */
    public String getTreeLog(){

        int[]   zero = new int[reachMatrix.length];
        Arrays.fill(zero, NAN);

        String log = "";

        for(int i=0; i<reachMatrix.length; i++){

            if(!Arrays.equals(reachMatrix[i], zero)){

                log = log.concat(String.format("<p></p><h3>Reachable states from S%s %s:</h3>", i, Arrays.toString(statesList.get(i))));

                for(int j=0; j<reachMatrix.length; j++){

                    if(reachMatrix[i][j] != NAN){

                        log = log.concat(String.format("<p>T%d => S%d %s</p>", reachMatrix[i][j], j, Arrays.toString(statesList.get(j))));

                    }
                }

            }
            else {
                log = log.concat(String.format("<p></p><h3 style=\"color:#8300004a\">Deadlock on S%s %s</h3>", i, Arrays.toString(statesList.get(i))));
            }

        }

        return log;
    }

    /**
     * Checks if the given state is already on the list
     * @param marking current marking of the node, it's equivalent to a state
     * @return int array where the first element is 1 if the state is repeated and 0 in the opposite case;
     * and the second element is the state number, regardless if it's repeated
     */
    int[] repeatedState(int[] marking){

        for(int i=0; i<statesList.size(); i++){
            if(Arrays.equals(statesList.get(i), marking)){
                return new int[]{1, i};
            }
        }

        statesList.add(marking);
        return new int[]{0, statesList.size()-1};
    }

    /**
     * Sets the shortest path to deadlock
     * @param path a path to deadlock
     */
    void setDeadLock(ArrayList<Integer> path){

        //Last transition is a -1 from the root, we just discard it
        path.remove(path.size()-1);

        if(!deadlock){
            SPDeadlock = path;
            deadlock = true;
        }
        else if(SPDeadlock.size() > path.size()){
            SPDeadlock = path;
        }

    }

    /**
     * Generates the resulting marking from firing a transition in certain state
     * @param transition number of transition to fire
     * @param marking current marking or state of the net
     * @return int array with the marking after firing the given transition
     */
    int[] fire(int transition, int[] marking){

        int[] resultMarking = new int[placeCount];

        for(int i=0; i<placeCount; i++){

            if(marking[i] != -1) {
                resultMarking[i] = iCombined[i][transition] + marking[i];
            }
            else{
                resultMarking[i] = marking[i];
            }
        }

        if(hasResetArcs){
            for(int i=0; i<placeCount; i++){
                if(reset[i][transition] != 0){
                    resultMarking[i] = 0;
                }
            }
        }

        return resultMarking;

    }


    /**
     * Calculates the enabled transitions based on incidence, inhibition and reader matrix.
     * @param state current marking of the net
     * @return boolean array with true for enabled transitions
     */
    boolean [] areTransitionsEnabled(int [] state){

        boolean [] enabledTransitions = new boolean[transitionCount];

        for(int i = 0; i<transitionCount; i++){

            enabledTransitions[i] = true;
            for(int j=0; j<placeCount ; j++){
                if ((iMinus[j][i] > state[j]) && state[j] != -1) {
                    enabledTransitions[i] = false;
                    break;
                }
            }

            if(hasInhibitionArcs){
                for(int j = 0; j < placeCount; j++){
                    if ((inhibition[j][i]>0 && state[j] >= inhibition[j][i]) || (inhibition[j][i] > 0 && state[j] == -1)) {
                        enabledTransitions[i] = false;
                        break;
                    }
                }
            }

            if(hasReaderArcs){
                for(int j=0; j<placeCount ; j++){
                    if(reader[j][i]>0 && reader[j][i] > state[j] && state[j] != -1){
                        enabledTransitions[i] = false;
                        break;
                    }
                }
            }

        }

        return enabledTransitions;

    }


    int getPlaceCount() {
        return placeCount;
    }

    int getTransitionCount(){
        return transitionCount;
    }

    TreeNode getRootNode() {
        return rootNode;
    }

    int[][] getInhibition(){
        return inhibition;
    }

    void setNotBounded(){
        bounded = false;
    }

    public boolean isBounded(){
        return bounded;
    }

    public boolean hasDeadlock(){
        return deadlock;
    }

    public ArrayList<Integer> getShortestPathToDeadlock(){
        return SPDeadlock;
    }

    public int[][] getReachabilityMatrix() {
        return reachMatrix;
    }


    /**
     * Checks if the given matrix is not null or all zeros
     */
    private boolean isMatrixNonZero(int[][] matrix){
        // if the matrix is null or if all elements are zeros
        // the net does not have the type of arcs described by the matrix semantics
        try{
            for (int[] ints : matrix) {
                for (int j = 0; j < matrix[0].length; j++) {
                    if (ints[j] != 0)
                        return true;
                }
            }
            return false;
        } catch (NullPointerException e){
            return false;
        }
    }

}
