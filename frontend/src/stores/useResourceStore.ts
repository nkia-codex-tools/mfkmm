import { create } from 'zustand';
import type { ResourceType } from '../types/resource';

interface ResourceState {
  activeTab: ResourceType;
  selectedRowIds: number[];
  searchQuery: string;
  setActiveTab: (tab: ResourceType) => void;
  setSelectedRows: (ids: number[]) => void;
  clearSelection: () => void;
  setSearchQuery: (query: string) => void;
}

export const useResourceStore = create<ResourceState>((set) => ({
  activeTab: 'functions',
  selectedRowIds: [],
  searchQuery: '',
  setActiveTab: (tab) => set({ activeTab: tab, selectedRowIds: [], searchQuery: '' }),
  setSelectedRows: (ids) => set({ selectedRowIds: ids }),
  clearSelection: () => set({ selectedRowIds: [] }),
  setSearchQuery: (query) => set({ searchQuery: query }),
}));
