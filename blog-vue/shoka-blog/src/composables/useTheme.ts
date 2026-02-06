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

export function useTheme() {
  const theme = ref<ThemeMode>(getStoredTheme());

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

  function setTheme(mode: ThemeMode) {
    theme.value = mode;
    localStorage.setItem(THEME_KEY, mode);
    applyTheme(mode);
  }

  onMounted(() => {
    applyTheme(theme.value);
  });

  watch(theme, (val) => {
    applyTheme(val);
  }, { immediate: true });

  return { theme, cycleTheme, setTheme };
}
