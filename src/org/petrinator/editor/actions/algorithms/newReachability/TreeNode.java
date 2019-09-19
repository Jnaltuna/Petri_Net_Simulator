package org.petrinator.editor.actions.algorithms.newReachability;

import java.util.ArrayList;

public class TreeNode {

    private TreeNode parent;
    private ArrayList<TreeNode> children;
    private int[] marking;
    private boolean[] enabledTransitions;
    private myTree tree;

    private int depth;
    private int id;

    private ArrayList<int[]> pathToDeadlock;
    private boolean deadlock;
    private boolean repeatedNode;

    TreeNode(myTree tree, int[] marking, TreeNode parent, int depth){

        this.marking = marking;
        this.parent = parent;
        this.depth = depth;
        this.tree = tree;
        children = new ArrayList<>();

        enabledTransitions = tree.areTransitionsEnabled(this.marking);
        deadlock = true;

        repeatedNode = tree.repeatedState(this.marking);
    }

    void recursiveExpansion(){

        if(repeatedNode){
            return;
        }

        for(int i=0; i<enabledTransitions.length; i++){

            if(enabledTransitions[i]){

                deadlock = false;
                children.add(new TreeNode(tree, tree.fire(i, marking), this, depth+1));
                children.get(children.size()-1).recursiveExpansion();

            }
        }

        if(deadlock){
            //System.out.println("Hay deadlock");
            recordDeadPath();
        }
    }

    private void recordDeadPath(){
        pathToDeadlock = new ArrayList<>();
        pathToDeadlock.add(marking);

        ArrayList<TreeNode> nodePath = new ArrayList<>();
        nodePath.add(this);

        for(int i=1; i<depth; i++){
            nodePath.add(nodePath.get(i-1).parent);
            pathToDeadlock.add(nodePath.get(i).getMarking());

        }

    }

    public int[] getMarking() {
        return marking;
    }

}
