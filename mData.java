import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.logging.Logger;

public class mData implements Serializable {
  protected static final Logger log = Logger.getLogger("Minecraft");
  private Hashtable balances;
  private Hashtable chests;
  private String file;
  private int startBalance;

  public mData(String paramString, int paramInt) throws IOException, ClassNotFoundException {
    mData localmData = null;
    this.file = paramString;
    this.startBalance = paramInt;
    try
    {
      FileInputStream localFileInputStream = new FileInputStream(this.file);
      ObjectInputStream localObjectInputStream = new ObjectInputStream(localFileInputStream);
      localmData = (mData)localObjectInputStream.readObject();
      localObjectInputStream.close();
      localFileInputStream.close();
    }
    catch (Exception localException)
    {
      localmData = null;
    }

    if (localmData == null)
    {
      this.balances = new Hashtable();
      this.chests = new Hashtable();
      write();
    }
    else
    {
      this.balances = localmData.balances;
      this.chests = localmData.chests;
    }
  }

  public void write()
  {
    try
    {
      FileOutputStream localFileOutputStream = new FileOutputStream(this.file);
      ObjectOutputStream localObjectOutputStream = new ObjectOutputStream(localFileOutputStream);
      localObjectOutputStream.writeObject(this);
      localObjectOutputStream.close();
      localFileOutputStream.close();
    }
    catch (Exception localException)
    {
      log.severe("[mData] Critical error while writing data: ");
      localException.printStackTrace();
    }
  }

  public int getBalance(String paramString)
  {
    if (this.balances.get(paramString) == null)
    {
      this.balances.put(paramString, Integer.valueOf(this.startBalance));
      return this.startBalance;
    }

    return ((Integer)this.balances.get(paramString)).intValue();
  }

  public void setBalance(String paramString, int paramInt)
  {
    this.balances.put(paramString, Integer.valueOf(paramInt));
  }

  public String getChest(String paramString)
  {
    if (this.chests.get(paramString) != null)
    {
      return (String)this.chests.get(paramString);
    }

    return null;
  }

  public void setChestOwner(String paramString, Block paramBlock)
  {
    this.chests.put(paramString, getChestName(paramBlock));
  }

  public String getChestName(Block paramBlock)
  {
    return Integer.toString(paramBlock.getX()) + Integer.toString(paramBlock.getY()) + Integer.toString(paramBlock.getZ());
  }

  public boolean hasChestAccess(String paramString, Block paramBlock)
  {
    log.info("Player: " + paramString + ", Chestblock:" + paramBlock);
    String str = getChestName(paramBlock);
    return (!this.chests.contains(str)) || (getChest(paramString).equals(str));
  }
}