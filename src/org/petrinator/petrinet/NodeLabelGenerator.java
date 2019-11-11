/*
 * Copyright (C) 2008-2010 Martin Riesz <riesz.martin at gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.petrinator.petrinet;

public class NodeLabelGenerator {

    private PetriNet petriNet;

    public NodeLabelGenerator(PetriNet petriNet) {
        this.petriNet = petriNet;
    }

    /*
     * Agregado. Crear etiqueta por defecto de un nuevo nodo. (P+id)
     */
    public void setLabelToNewlyCreatedNode(Node node) 
    {
        if(node instanceof Place)
        	node.setLabel(node.getId());
        else if(node instanceof Transition)
        	node.setLabel(node.getId());
        else
        	node.setLabel("");
    }

    public void setLabelsOfConversionTransitionToSubnet(Transition transition, Subnet subnet) {

        subnet.setLabel(transition.getLabel());
        transition.setLabel(null);
    }

}
