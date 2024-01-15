package LLVMIR.Value.Instruction;

import LLVMIR.Type.Type;
import LLVMIR.Value.BasicBlock;
import LLVMIR.Value.User;
import LLVMIR.Value.Value;
import Utils.DataStruct.IList;

import java.util.ArrayList;

public class Instruction extends User {
    private OP op;
    private boolean hasname = false;

    public boolean hasname() {
        return hasname;
    }

    public void setHasname(boolean hasname) {
        this.hasname = hasname;
    }

    private final IList.INode<Instruction, BasicBlock> node;

    public Instruction(String name,Type type,OP op) {
        super(name,type);
        this.op = op;
        this.node = new IList.INode<>(this);
    }

    public IList.INode<Instruction, BasicBlock> getNode() {
        return node;
    }

    public OP getOp() {
        return op;
    }

    public ArrayList<Value> getUseValues() {
        return getOperands();
    }

    public void setOp(OP op) {
        this.op = op;
    }
    public BasicBlock getParentbb(){
        return node.getParent().getValue();
    }

    public String getInstString(){
        return "";
    }

    //!----inst insert before this------------------------------------------------------------
    public void insertBefore(Instruction instruction){
        node.insertBefore(instruction.getNode());
    }

    public void insertAfter(Instruction instruction){
        node.insertAfter(instruction.getNode());
    }

    public void insertToHead(BasicBlock basicBlock) {
        node.insertListHead(basicBlock.getInsts());
    }

    public boolean hasName(){
        return true;
    }

    public void removeSelf(){
        removeUseFromOperands();
        node.removeFromList();
    }

    public void removeFromBasicBlock(){
        node.removeFromList();
    }

}
