package hardcorequesting.api;

import hardcorequesting.api.reward.IReward;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public abstract class DefaultQuest implements IQuest{
    
    private IQuestline questline;
    private UUID questId, parentId;
    private String nameTranslationKey, descTranslationKey;
    private List<ITask> tasks;
    private List<IHook> hooks;
    private List<IReward> rewards;
    private int posX, posY;
    
    @Nonnull
    private ItemStack renderIcon = ItemStack.EMPTY;
    
    @Override
    public void onCreation(IQuestline questline, UUID questId, NBTTagCompound additionalData, List<ITask> tasks, List<IHook> hooks, List<IReward> rewards){
        this.questline = questline;
        this.questId = questId;
        this.parentId = additionalData.getUniqueId("Parent");
        this.nameTranslationKey = additionalData.getString("Name");
        this.descTranslationKey = additionalData.getString("Desc");
        this.tasks = tasks;
        this.hooks = hooks;
        this.rewards = rewards;
        this.posX = additionalData.getInteger("X");
        this.posY = additionalData.getInteger("Y");
    }
    
    @Nonnull
    @Override
    public IQuestline getQuestline(){
        return this.questline;
    }
    
    @Nonnull
    @Override
    public UUID getUUID(){
        return this.questId;
    }
    
    @Nullable
    @Override
    public UUID getParentUUID(){
        return this.parentId;
    }
    
    @Nonnull
    @Override
    public String getNameTranslationKey(){
        return this.nameTranslationKey;
    }
    
    @Nullable
    @Override
    public String getDescTranslationKey(){
        return this.descTranslationKey;
    }
    
    @Nonnull
    @Override
    public NBTTagCompound getAdditionalData(){
        NBTTagCompound data = new NBTTagCompound();
        if(this.getParentUUID() != null){
            data.setUniqueId("Parent", this.getParentUUID());
        }
        data.setString("Name", this.getNameTranslationKey());
        if(this.getDescTranslationKey() != null){
            data.setString("Desc", this.getDescTranslationKey());
        }
        data.setInteger("X", this.getX());
        data.setInteger("Y", this.getY());
        return data;
    }
    
    @Override
    public List<ITask> getTasks(){
        return this.tasks;
    }
    
    @Override
    public List<IHook> getHooks(){
        return this.hooks;
    }
    
    @Override
    public List<IReward> getRewards(){
        return rewards;
    }
    
    @Override
    public int getX(){
        return this.posX;
    }
    
    @Override
    public int getY(){
        return this.posY;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void renderIcon(){
        //todo
    }
    
    public void setRenderItemStack(@Nonnull ItemStack stack){
        this.renderIcon = stack;
    }
}