package LLVMIR.Value;

import LLVMIR.Type.LableType;
import LLVMIR.Value.Instruction.Instruction;
import Utils.DataStruct.IList;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class BasicBlock extends Value{
    private Function parentFunction;
    private ArrayList<BasicBlock> preBlocks;
    private ArrayList<BasicBlock> nxtBlocks;
    public final ArrayList<Value> liveIns=new ArrayList<>();
    public final ArrayList<Value> liveOuts=new ArrayList<>();
    public final ArrayList<Value> Use=new ArrayList<>();
    public final ArrayList<Value> Def=new ArrayList<>();
    private final IList<Instruction, BasicBlock> insts;
    public static int blockNum = 0;
    private final IList.INode<BasicBlock, Function> node;
    private ArrayList<BasicBlock> idoms;
    private BasicBlock idominator;
//    private IRLoop loop;
//    private int domLV;

    public final ArrayList<ArrayList<Value>> LocalInterfere = new ArrayList<>();


    public BasicBlock(){
        super("block"+ ++blockNum,new LableType());
        this.insts = new IList<>(this);
        this.preBlocks = new ArrayList<>();
        this.nxtBlocks = new ArrayList<>();
        this.node = new IList.INode<>(this);
    }
    public BasicBlock(Function function){
        super("block"+ ++blockNum,new LableType());
        this.insts = new IList<>(this);
        this.preBlocks = new ArrayList<>();
        this.nxtBlocks = new ArrayList<>();
        this.node = new IList.INode<>(this);
        this.parentFunction = function;


    }



    //!---------get-------------------------------------------------------
    public IList.INode<BasicBlock, Function> getNode() {
        return node;
    }

    public IList<Instruction, BasicBlock> getInsts() {
        return insts;
    }

    public ArrayList<ArrayList<Value>> getLocalInterfere() {
        return LocalInterfere;
    }

    public ArrayList<BasicBlock> getNxtBlocks() {
        return nxtBlocks;
    }

    public ArrayList<BasicBlock> getIdoms() {
        return idoms;
    }

    public ArrayList<BasicBlock> getPreBlocks() {
        return preBlocks;
    }

    public ArrayList<Value> getDef() {
        return Def;
    }

    public ArrayList<Value> getLiveIns() {
        return liveIns;
    }

    public ArrayList<Value> getLiveOuts() {
        return liveOuts;
    }

    public ArrayList<Value> getUse() {
        return Use;
    }

    public BasicBlock getIdominator() {
        return idominator;
    }

    public Function getParentFunction() {
        return parentFunction;
    }
    public void addInst(Instruction inst){
        inst.getNode().insertListEnd(insts);
    }

    public void addInstToHead(Instruction inst){
        inst.getNode().insertListHead(insts);
    }

    public static int getBlockNum() {
        return blockNum;
    }

    //!-------------set---------------------------------------------------


    //
    public void setPreBlocks(ArrayList<BasicBlock> preBlocks) {

        //TODO judge the block items
        this.preBlocks = preBlocks;
    }

    public void setNxtBlocks(ArrayList<BasicBlock> nxtBlocks) {
        this.nxtBlocks = nxtBlocks;
    }

    public void setPreBlock(BasicBlock preBlock){
        if(preBlocks!=null&& !preBlocks.contains(preBlock)){
            preBlocks.add(preBlock);
        }
    }
    public void setNxtBlock(BasicBlock nxtBlock){
        if(nxtBlocks!=null&& !nxtBlocks.contains(nxtBlock)){
            nxtBlocks.add(nxtBlock);
        }
    }

    public void removePreBlock(BasicBlock preblock){
        preBlocks.remove(preblock);
    }
    public void removeNxtBlock(BasicBlock nxtblock){
        nxtBlocks.remove(nxtblock);
    }



}
