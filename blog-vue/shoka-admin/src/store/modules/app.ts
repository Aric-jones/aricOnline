import { defineStore } from "pinia";
import { AppState } from "../interface";

const useAppStore = defineStore("useAppStore", {
  state: (): AppState => ({
    isCollapse: false,
    device: "desktop",
    size: "default",
    routeLoading: false,
  }),
  actions: {
    setRouteLoading(loading: boolean) {
      this.routeLoading = loading;
    },
    toggle() {
      this.isCollapse = !this.isCollapse;
    },
    changeCollapse(isCollapse: boolean) {
      this.isCollapse = isCollapse;
    },
    toggleDevice(device: string) {
      this.device = device;
    },
    setSize(size: string) {
      this.size = size;
    },
  },
  getters: {},
  persist: {
    storage: localStorage,
    pick: ["isCollapse", "device", "size"],
  },
});

export default useAppStore;
