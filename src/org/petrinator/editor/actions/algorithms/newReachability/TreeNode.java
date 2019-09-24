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
    private boolean repeatedNode;


    public TreeNode(CRTree tree, int[] marking, TreeNode parent, int depth){

        this.marking = marking;
        this.parent = parent;
        this.depth = depth;
        this.tree = tree;
        children = new ArrayList<>();

        id = nodes;
        nodes++;

        enabledTransitions = tree.areTransitionsEnabled(this.marking);
        deadlock = true;

        repeatedNode = tree.repeatedState(this.marking);
    }

    String getNodeId(){
        return "E"+id;
    }

    void recursiveExpansion(){

        //TODO si se quiere saber todos los caminos preguntar adentro del for
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
            System.out.println("Hay deadlock");
            recordDeadPath();
            tree.setDeadLock(pathToDeadlock);
            /*for(int i=pathToDeadlock.size()-1; i>=0; i--){
                System.out.println(Arrays.toString(pathToDeadlock.get(i)));
            }*/
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

    public int[] getMarking() {
        return marking;
    }

}
