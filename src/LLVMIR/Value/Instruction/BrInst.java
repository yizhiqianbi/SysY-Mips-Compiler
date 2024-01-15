package LLVMIR.Value.Instruction;

import LLVMIR.Type.Type;
import LLVMIR.Type.VoidType;
import LLVMIR.Value.BasicBlock;
import LLVMIR.Value.Value;

public class BrInst extends Instruction{
    private int state;
    private BasicBlock TrueBlock;
    private BasicBlock FalseBlock;
    private BasicBlock JumpBlock;





    public BrInst(BasicBlock jumpBlock){
        super("", VoidType.voidType,OP.Br);
        this.JumpBlock = jumpBlock;
        addOperand(jumpBlock);
        this.state = 1;
    }

    public BrInst(Value value,BasicBlock iftrueBlock,BasicBlock elseBlock){
        super("", VoidType.voidType, OP.Br);
        addOperand(value);
        addOperand(iftrueBlock);
        addOperand(elseBlock);
        this.TrueBlock = iftrueBlock;
        this.FalseBlock = elseBlock;
        this.state = 2;
    }

    public Value getJudVal(){
        return getOperand(0);
    }

    public BasicBlock getTrueBlock() {
        return TrueBlock;
    }

    public BasicBlock getFalseBlock() {
        return FalseBlock;
    }

    public BasicBlock getJumpBlock() {
        return JumpBlock;
    }

    public void setTrueBlock(BasicBlock trueBlock) {
        TrueBlock = trueBlock;
    }

    public void setFalseBlock(BasicBlock falseBlock) {
        FalseBlock = falseBlock;
    }

    public void setJumpBlock(BasicBlock jumpBlock) {
        JumpBlock = jumpBlock;
    }


    public int getState() {
        return state;
    }

    @Override
    public String getInstString(){
        StringBuilder sb = new StringBuilder();
        if(state == 2) {
            sb.append("br ");
            sb.append(getJudVal()).append(", ");
            sb.append("label ").append(getTrueBlock().getName()).append(", ");
            sb.append("label ").append(getFalseBlock().getName());
        }
        //  直接跳转
        else {
            sb.append("br label ");
            sb.append(getJumpBlock().getName());
        }
        return sb.toString();
    }
}
