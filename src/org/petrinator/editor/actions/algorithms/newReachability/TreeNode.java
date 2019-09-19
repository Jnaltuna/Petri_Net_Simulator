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

    private boolean deadlock;
    private boolean repeatedNode;

    public TreeNode(myTree tree, int[] marking, TreeNode parent, int depth){

        this.marking = marking;
        this.parent = parent;
        this.depth = depth;
        this.tree = tree;
        children = new ArrayList<>();

        enabledT = tree.enabled(this.marking);
        deadlock = true;

        repeatedNode = tree.repeatedState(this.marking);
    }

    public void recursiveExpansion(){

        if(repeatedNode){
            return;
        }

        for(int i=0; i<enabledTransitions.length; i++){

            if(enabledTransitions[i]){

                deadlock = false;
                children.add(new TreeNode(tree, tree.fire(i, marking), this, depth+1));

            }
        }

        if(deadlock){
            System.out.println("HAY deadlock PAPU");
        }


    }

}
