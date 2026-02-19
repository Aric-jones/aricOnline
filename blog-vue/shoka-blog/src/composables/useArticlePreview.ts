import { ref } from 'vue';

const articlePreviewRef = ref<any>(null);

export function useArticlePreview() {
  return { articlePreviewRef };
}
