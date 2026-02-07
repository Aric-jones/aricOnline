import { ref, watch, onMounted } from 'vue';

export type ThemeMode = 'light' | 'dark' | 'gradient';

const THEME_KEY = 'theme';

function getStoredTheme(): ThemeMode {
  if (typeof window === 'undefined') return 'light';
  const stored = localStorage.getItem(THEME_KEY) as ThemeMode | null;
  if (stored === 'light' || stored === 'dark' || stored === 'gradient') return stored;
  return 'light';
}

function applyTheme(mode: ThemeMode) {
  if (typeof document === 'undefined') return;
  document.documentElement.setAttribute('theme', mode);
}

// 全站共用同一个 theme ref，切换主题时所有使用 useTheme() 的组件都会更新
let themeRef: ReturnType<typeof ref<ThemeMode>> | null = null;

export function useTheme() {
  if (!themeRef) {
    themeRef = ref<ThemeMode>(getStoredTheme());
    watch(themeRef, (val) => {
      applyTheme(val);
    }, { immediate: true });
  }
  const theme: ReturnType<typeof ref<ThemeMode>> = themeRef;

  function cycleTheme() {
    const next: Record<ThemeMode, ThemeMode> = {
      light: 'dark',
      dark: 'gradient',
      gradient: 'light',
    };
    theme.value = next[theme.value];
    localStorage.setItem(THEME_KEY, theme.value);
    applyTheme(theme.value);
  }


  onMounted(() => {
    applyTheme(theme.value);
  });

  return { theme, cycleTheme};
}
