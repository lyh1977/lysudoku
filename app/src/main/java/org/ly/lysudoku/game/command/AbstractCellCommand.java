package org.ly.lysudoku.game.command;

import org.ly.lysudoku.Grid;

import java.util.BitSet;
import java.util.StringTokenizer;

/**
 * Generic command acting on one or more cells.
 *
 * @author romario
 */
public abstract class AbstractCellCommand extends AbstractCommand {
    public AbstractCellCommand(Grid cells) {
        this.mCells = cells;
    }
    public AbstractCellCommand() {

    }
    private Grid mCells;

    protected Grid getCells() {
        return mCells;
    }

    public void setCells(Grid mCells) {

        this.mCells = mCells;
    }
    public static void serializeBitSet(StringBuilder data, BitSet bs) {
        data.append(10);
        data.append("|");
        long notedNumbers = 0;
        for (int i=0;i<10;i++)
        {
            if(bs.get(i))
                notedNumbers = (int) (notedNumbers | (1 << i));
        }
        data.append(notedNumbers);
        data.append("|");
    }
    public static String serializeBitSet( BitSet bs) {
        StringBuilder sb = new StringBuilder();
        serializeBitSet(sb,bs);
        return sb.toString();
    }
    public static BitSet deserializeBitset(StringTokenizer data) {
        int len=Integer.parseInt( data.nextToken());
        long  longBit=Long.parseLong(data.nextToken());
        BitSet bit=new BitSet(len);
        int c=1;
        for(int i=0;i<len ;i++)
        {
            if ((longBit & (int) c) != 0) {
                bit.set(i,true);
            }
            else {
                bit.set(i,false);
            }
            c = (c << 1);
        }
        return bit;
    }
}
