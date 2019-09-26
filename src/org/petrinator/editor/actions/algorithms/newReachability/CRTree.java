package org.petrinator.editor.actions.algorithms.newReachability;

import org.petrinator.editor.Root;
import pipe.exceptions.TreeTooBigException;

import java.util.ArrayList;
import java.util.Arrays;

public class CRTree {

    public static final int REPEATED = 0;
    public static final int STATE = 1;

    private boolean foundAnOmega = false;            //bounded
    private boolean moreThanOneToken = false;        //safe

    private boolean deadlock = false;    //deadlock
    private ArrayList<Integer> shortestPathToDead;

    private ArrayList<int[]> statesList;

    private TreeNode root;                             //root of the tree
    private int nodeCount = 0;                       //total number of nodes

    //Petri net matrices: TODO : final?
    private int [][] iPlus;
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

    private int[] pathToDeadlock;
    private final boolean tooBig = false;
    private int edges = 0;
    private int states = 0;

    private String log;

    private final Root petri_root;

    public CRTree(Root petri_root, int[] initialMarking) throws TreeTooBigException{

        this.petri_root = petri_root;

        iPlus = petri_root.getDocument().getPetriNet().forwardIMatrix();
        iMinus = petri_root.getDocument().getPetriNet().backwardsIMatrix();
        iCombined = petri_root.getDocument().getPetriNet().incidenceMatrix();
        inhibition = petri_root.getDocument().getPetriNet().inhibitionMatrix();
        reset = petri_root.getDocument().getPetriNet().resetMatrix();
        reader = petri_root.getDocument().getPetriNet().readerMatrix();


        hasInhibitionArcs = isMatrixNonZero(inhibition);
        hasReaderArcs = isMatrixNonZero(reader);
        hasResetArcs = isMatrixNonZero(reset);

        //TODO add capacity/priority/timed if needed

        transitionCount = iMinus[0].length;
        placeCount = iMinus.length;//TODO view if values are right

        statesList = new ArrayList<>();

        root = new TreeNode(this, initialMarking, -1, root, 0);

        //this.moreThanOneToken = isSafe(treeRoot);
        repeatedState(initialMarking); //add initial marking to state list

        root.recursiveExpansion();

        System.out.printf("STATES - %d\n", statesList.size());
        for(int i=0; i<statesList.size(); i++){
            for(int j=0; j<placeCount; j++){
                System.out.printf("%2d ", statesList.get(i)[j]);
            }
            System.out.println("");
        }

        log = root.recursiveLog();

        if(deadlock){
            System.out.println("Shortest Path to DeadLock: ");
            for(int i=shortestPathToDead.size()-1; i>=0; i--){
                System.out.printf("T%d -> ", shortestPathToDead.get(i));
            }
            System.out.println("Deadlock");
        }


    }

    public String getTreeLog(){
        return log;
    }

    /**
     *
     * @param marking current marking of the node, it's equivalent to a state
     * @return a vector where the first element is 1 if the state is repeated and 0 in the opposite case;
     * and the second element is the state number, regardless if it's repeated
     */
    int[] repeatedState(int[] marking){

        for(int i=0; i<statesList.size(); i++){
            if(Arrays.equals(statesList.get(i), marking)){
                return new int[]{1, i};
            }
        }
        System.out.println(Arrays.toString(marking));
        statesList.add(marking);
        return new int[]{0, statesList.size()-1};
    }

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

        if(hasResetArcs){ //TODO view if we need to consider omegas
            for(int i=0; i<placeCount; i++){
                if(reset[i][transition] != 0){
                    resultMarking[i] = 0;
                }
            }
        }

        return resultMarking;

    }

    void setDeadLock(ArrayList<Integer> path){

        //Last transition is a -1 from the root, we just discard it
        path.remove(path.size()-1);

        if(!deadlock){
            shortestPathToDead = path;
            deadlock = true;
        }
        else if(shortestPathToDead.size() > path.size()){
            shortestPathToDead = path;
        }

    }

    public int getPlaceCount() {
        return placeCount;
    }

    public int getTransitionCount(){
        return transitionCount;
    }

    public TreeNode getRoot() {
        return root;
    }

    public int[][] getInhibition(){
        return inhibition;
    }

    public void setFoundAnOmega(){
        foundAnOmega = true;
    }


    /*public CRTree(Root petri_root, int[] treeRoot, File reachabilityGraph)

            throws TreeTooBigException, ImmediateAbortException
    {
        this.petri_root = petri_root;

        _CPlus = petri_root.getDocument().getPetriNet().forwardIMatrix();
        _CMinus = petri_root.getDocument().getPetriNet().backwardsIMatrix();
        _C = petri_root.getDocument().getPetriNet().incidenceMatrix();
        _inhibition = petri_root.getDocument().getPetriNet().inhibitionMatrix();
        _reset = petri_root.getDocument().getPetriNet().resetMatrix();
        _reader = petri_root.getDocument().getPetriNet().readerMatrix();

        //TODO add capacity/priority/timed if needed

        transitionCount = _CMinus[0].length;
        placeCount = _CMinus.length;//TODO view if values are right

        root = new TreeNode(this, treeRoot, root, 1); //TODO view if tree reference needed

        //this.moreThanOneToken = isSafe(treeRoot);

        RandomAccessFile outputFile;
        RandomAccessFile esoFile;
        File intermediate = new File("graph.irg");

        if(intermediate.exists()){
            if(!intermediate.delete()){
                System.err.println("Could not delete intermediate file.");
            }
        }

        try
        {
            outputFile = new RandomAccessFile(intermediate, "rw");
            esoFile = new RandomAccessFile(reachabilityGraph, "rw");
            // Write a blank file header as a place holder for later
            ReachabilityGraphFileHeader header = new ReachabilityGraphFileHeader();
            header.write(esoFile);
            //Call expansion function on root of tree
            //TODO createCoverabilityGraph(outputFile, esoFile);
            outputFile.close();
        }
        catch(IOException e)
        {
            System.err.println("Could not create intermediate files.");
            return;
        }

       //TODO createCGFile(intermediate, esoFile, treeRoot.length, states, edges);

        if(intermediate.exists())
        {
            if(!intermediate.delete())
            {
                System.err.println("Could not delete intermediate file.");
            }
        }

    }

    //Determines if any place has more than one token for the current marking
    /*private boolean isSafe(final int[] treeRoot)
    {
        for(int aTreeRoot : treeRoot)
        {
            if(aTreeRoot > 1)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Function: void RecursiveExpansion()
     * Undertakes a recursive expansion of the tree
     * Called on root node from within the tree constructor.
     * @param outputFile
     * @param esoFile
     * @throws pipe.exceptions.TreeTooBigException
     * @throws pipe.io.ImmediateAbortException
     */
    /*private void createCoverabilityGraph(RandomAccessFile outputFile,
                                         RandomAccessFile esoFile) throws TreeTooBigException, pipe.io.ImmediateAbortException
    {
        int[] newMarkup; //mark used to create new node

        boolean repeatedNode; //attribute used for

        //int[] state = new int[placeCount];
        int[] state = root.getMarking();

        boolean[] enabledTransitions = areTransitionsEnabled(state);

        //writeNode(root.getID(),root.getMarking(),esoFile,true); TODO implementar ID
        states++;

        ArrayList<TreeNode> unprocessednodes = new ArrayList();

        unprocessednodes.add(root);
        TreeNode currentNode;
        while(!unprocessednodes.isEmpty()){

            //TODO sacar break
            break;
            /*
            currentNode = unprocessednodes.get(0);
            unprocessednodes.remove(0);

            state = currentNode.getMarking();

            enabledTransitions = areTransitionsEnabled(state);

            for (int i = 0; i< enabledTransitions.length; i++){
                if(enabledTransitions[i]){

                }
            }
        }
    }*/

    /**
     *
     * @param state current marking of the net
     * @return boolean array with true for enabled transitions
     */
    boolean [] areTransitionsEnabled(int [] state){

        boolean [] enabledTranitions = new boolean[transitionCount];

        for(int i = 0; i<transitionCount; i++){
            //for que recorre cada transicion
            enabledTranitions[i] = true;
            //comparo incidencia con marca
            for(int j=0; j<placeCount ; j++){
                if ((iMinus[j][i] > state[j]) && state[j] != -1) {
                    enabledTranitions[i] = false;
                    break;
                }
            }

            if(hasInhibitionArcs){
                for(int j = 0; j < placeCount; j++){
                    boolean emptyPlace = state[j] == 0;
                    boolean placeInhibitsTransition = inhibition[j][i] != 0;
                    if ((inhibition[j][i]>0 && state[j] >= inhibition[j][i]) || (inhibition[j][i] > 0 && state[j] == -1)) {
                        enabledTranitions[i] = false;
                        break;
                    }
                }
            }

            if(hasReaderArcs){
                for(int j=0; j<placeCount ; j++){
                    //if (reader[j][i] > state[j]) {
                    if(reader[j][i]>0 && reader[j][i] > state[j] && state[j] != -1){ //check logic
                        enabledTranitions[i] = false;
                        break;
                    }
                }
            }

        }
        return enabledTranitions;

    }

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

    public boolean isFoundAnOmega() {
        return foundAnOmega;
    }

}
