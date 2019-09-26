package org.petrinator.editor.actions.algorithms.newReachability;

import java.util.ArrayList;
import java.util.Arrays;

public class TreeNode {

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

        enabledTransitions = tree.areTransitionsEnabled(this.marking);
        deadlock = true;

        int[] rs = tree.repeatedState(this.marking).clone();

        repeatedState = (rs[CRTree.REPEATED] == 1);
        id = rs[CRTree.STATE];

    }

    String getNodeId(){
        return String.format("S%-4d",id);
    }

    void recursiveExpansion(){

        //TODO si se quiere saber todos los caminos preguntar adentro del for
        if(repeatedState){
            return;
        }

        for(int i=0; i<enabledTransitions.length; i++){

            if(enabledTransitions[i]){

                deadlock = false;
                children.add(new TreeNode(tree, tree.fire(i, marking), i+1,this, depth+1));
                children.get(children.size()-1).recursiveExpansion();

            }
        }

        if(deadlock){
            System.out.println("Hay deadlock");
            recordDeadPath();
            tree.setDeadLock(pathToDeadlock);
        }
    }

    String recursiveLog(){

        String log = "";

        int childrenCount = children.size();

        if(childrenCount > 0){
            for(int i=0; i<childrenCount; i++){
                log = log.concat(children.get(i).recursiveLog());
            }

            log = log.concat(String.format("<p></p><h3>Reachable states from %3s %s:</h3>", getNodeId(), Arrays.toString(marking)));

            for (int j=0; j<childrenCount; j++){

                log = log.concat(String.format("<p>T%d => %s %s</p>", children.get(j).fromTransition ,children.get(j).getNodeId(), Arrays.toString(children.get(j).marking)));

            }

        }

        return log;

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

    public int[] getMarking() {
        return marking;
    }

}
