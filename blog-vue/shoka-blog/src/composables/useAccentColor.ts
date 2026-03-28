import { ref, computed, onMounted } from 'vue';

export type AccentColor = 'purple' | 'blue' | 'green';

interface AccentConfig {
  primary: string;
  primaryLight: string;
  rgb: string;
  label: string;
}

const ACCENT_MAP: Record<AccentColor, AccentConfig> = {
  purple: { primary: '#6366f1', primaryLight: '#818cf8', rgb: '99,102,241', label: '紫色' },
  blue:   { primary: '#3b82f6', primaryLight: '#60a5fa', rgb: '59,130,246', label: '蓝色' },
  green:  { primary: '#10b981', primaryLight: '#34d399', rgb: '16,185,129', label: '绿色' },
};

const CYCLE_ORDER: AccentColor[] = ['purple', 'blue', 'green'];
const STORAGE_KEY = 'todo-accent-color';

function getStored(): AccentColor {
  if (typeof window === 'undefined') return 'green';
  const v = localStorage.getItem(STORAGE_KEY) as AccentColor | null;
  return v && ACCENT_MAP[v] ? v : 'green';
}

function applyToDOM(color: AccentColor) {
  if (typeof document === 'undefined') return;
  const cfg = ACCENT_MAP[color];
  const root = document.documentElement;
  root.style.setProperty('--todo-primary', cfg.primary);
  root.style.setProperty('--todo-primary-light', cfg.primaryLight);
  root.style.setProperty('--todo-primary-rgb', cfg.rgb);
}

const accentRef = ref<AccentColor>(getStored());
applyToDOM(accentRef.value);

export function useAccentColor() {
  const accent = accentRef;

  function setAccent(color: AccentColor) {
    accent.value = color;
    localStorage.setItem(STORAGE_KEY, color);
    applyToDOM(color);
  }

  function cycleAccent() {
    const idx = CYCLE_ORDER.indexOf(accent.value);
    const next = CYCLE_ORDER[(idx + 1) % CYCLE_ORDER.length];
    accent.value = next;
    localStorage.setItem(STORAGE_KEY, next);
    applyToDOM(next);
  }

  const currentAccentColor = computed(() => ACCENT_MAP[accent.value].primary);
  const currentAccentLabel = computed(() => ACCENT_MAP[accent.value].label);

  onMounted(() => applyToDOM(accent.value));

  return { accent, setAccent, cycleAccent, currentAccentColor, currentAccentLabel };
}
