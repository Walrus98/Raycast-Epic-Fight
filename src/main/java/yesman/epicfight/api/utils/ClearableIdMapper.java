package yesman.epicfight.api.utils;

import net.minecraft.core.IdMapper;

public class ClearableIdMapper<I> extends IdMapper<I> {
	public ClearableIdMapper() {
		super(512);
	}
	
	public ClearableIdMapper(int size) {
		super(size);
	}
	
	public void clear() {
		this.tToId.clear();
		this.idToT.clear();
		this.nextId = 0;
	}
}