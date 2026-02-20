<template>
  <n-config-provider :theme="naiveTheme">
    <n-dialog-provider>
      <n-notification-provider>
        <n-message-provider>
          <slot></slot>
          <naive-provider-content />
        </n-message-provider>
      </n-notification-provider>
    </n-dialog-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { useDialog, useMessage, useNotification, darkTheme } from "naive-ui";
import { defineComponent, h, computed } from 'vue';
import { useTheme } from "@/composables/useTheme";

const { theme } = useTheme();
const naiveTheme = computed(() => (theme.value === "dark" ? darkTheme : null));

const registerNaiveTools = () => {
  window.$dialog = useDialog();
  window.$message = useMessage();
  window.$notification = useNotification();
};
const NaiveProviderContent = defineComponent({
  name: 'NaiveProviderContent',
  setup() {
    registerNaiveTools();
  },
  render() {
    return h('div');
  }
});
</script>
<style scoped></style>
