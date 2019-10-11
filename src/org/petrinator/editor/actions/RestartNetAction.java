package org.petrinator.editor.actions;

import org.petrinator.editor.Root;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RestartNetAction extends AbstractAction {

    private Root root;

    public RestartNetAction(Root root){
        this.root = root;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {

        root.getDocument().getPetriNet().getInitialMarking().resetMarking();

    }
}
