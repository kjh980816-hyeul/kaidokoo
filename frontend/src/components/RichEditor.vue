<script setup lang="ts">
import { ref, watch, onBeforeUnmount } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import { uploadPostAudio } from '../api/posts'
import StarterKit from '@tiptap/starter-kit'
import Underline from '@tiptap/extension-underline'
import TextStyle from '@tiptap/extension-text-style'
import Color from '@tiptap/extension-color'
import TextAlign from '@tiptap/extension-text-align'
import Link from '@tiptap/extension-link'
import { Extension, Node, mergeAttributes } from '@tiptap/core'

const props = defineProps<{ modelValue: string }>()
const emit = defineEmits<{ 'update:modelValue': [value: string] }>()

// 커스텀 FontSize: TextStyle의 인라인 style="font-size:..." 속성으로 글자 크기를 다룬다.
// (유지보수 불확실한 외부 패키지 대신 TipTap 문서 패턴으로 직접 구현)
declare module '@tiptap/core' {
  interface Commands<ReturnType> {
    fontSize: {
      setFontSize: (size: string) => ReturnType
      unsetFontSize: () => ReturnType
    }
    fontFamily: {
      setFontFamily: (family: string) => ReturnType
      unsetFontFamily: () => ReturnType
    }
  }
}

const FontSize = Extension.create({
  name: 'fontSize',
  addOptions() {
    return { types: ['textStyle'] }
  },
  addGlobalAttributes() {
    return [
      {
        types: this.options.types as string[],
        attributes: {
          fontSize: {
            default: null,
            parseHTML: (element: HTMLElement) => element.style.fontSize || null,
            renderHTML: (attributes: { fontSize?: string | null }) => {
              if (!attributes.fontSize) return {}
              return { style: `font-size: ${attributes.fontSize}` }
            },
          },
        },
      },
    ]
  },
  addCommands() {
    return {
      setFontSize:
        (size: string) =>
        ({ chain }) =>
          chain().setMark('textStyle', { fontSize: size }).run(),
      unsetFontSize:
        () =>
        ({ chain }) =>
          chain().setMark('textStyle', { fontSize: null }).removeEmptyTextStyle().run(),
    }
  },
})

// 커스텀 FontFamily: FontSize와 동일 패턴. textStyle에 인라인 style="font-family:..."를 건다.
const FontFamily = Extension.create({
  name: 'fontFamily',
  addOptions() {
    return { types: ['textStyle'] }
  },
  addGlobalAttributes() {
    return [
      {
        types: this.options.types as string[],
        attributes: {
          fontFamily: {
            default: null,
            parseHTML: (element: HTMLElement) => element.style.fontFamily || null,
            renderHTML: (attributes: { fontFamily?: string | null }) => {
              if (!attributes.fontFamily) return {}
              return { style: `font-family: ${attributes.fontFamily}` }
            },
          },
        },
      },
    ]
  },
  addCommands() {
    return {
      setFontFamily:
        (family: string) =>
        ({ chain }) =>
          chain().setMark('textStyle', { fontFamily: family }).run(),
      unsetFontFamily:
        () =>
        ({ chain }) =>
          chain().setMark('textStyle', { fontFamily: null }).removeEmptyTextStyle().run(),
    }
  },
})

// 영상 임베드 노드: 일반 <iframe>을 그대로 노드로 다룬다.
// (TipTap 기본 Youtube 확장은 <div data-youtube-video> 표식이 있어야만 재인식하는데,
//  서버 정화기가 그 data 속성을 떼어내 수정 화면에서 영상이 사라지는 버그가 있었다.
//  표식 없이 iframe만으로 왕복되도록 parseHTML을 'iframe'으로 잡는다. 유튜브·비메오 공용.)
const Iframe = Node.create({
  name: 'iframe',
  group: 'block',
  atom: true,
  selectable: true,
  draggable: true,
  addAttributes() {
    return {
      src: { default: null },
      width: { default: '640' },
      height: { default: '360' },
      frameborder: { default: '0' },
      allow: { default: 'autoplay; fullscreen; picture-in-picture; encrypted-media' },
      allowfullscreen: { default: 'true' },
    }
  },
  parseHTML() {
    return [{ tag: 'iframe' }]
  },
  renderHTML({ HTMLAttributes }) {
    return ['iframe', mergeAttributes(HTMLAttributes)]
  },
})

// 음악(오디오) 노드: 업로드한 mp3 등을 <audio controls>로 본문에 삽입한다.
// Iframe과 같은 원리 — 서버 정화 후에도 살아남는 <audio> 태그 자체를 노드로 잡아
// 저장·수정 왕복에서 표식 의존 없이 보존된다. (정화기는 /uploads/posts/ src만 허용)
const AudioNode = Node.create({
  name: 'audio',
  group: 'block',
  atom: true,
  selectable: true,
  draggable: true,
  addAttributes() {
    return {
      src: { default: null },
      controls: { default: 'controls' },
      preload: { default: 'metadata' },
    }
  },
  parseHTML() {
    return [{ tag: 'audio' }]
  },
  renderHTML({ HTMLAttributes }) {
    return ['audio', mergeAttributes(HTMLAttributes)]
  },
})

const editor = useEditor({
  content: props.modelValue,
  extensions: [
    StarterKit,
    Underline,
    TextStyle,
    Color,
    FontSize,
    FontFamily,
    Iframe,
    AudioNode,
    TextAlign.configure({ types: ['heading', 'paragraph'] }),
    Link.configure({ openOnClick: false, autolink: true, HTMLAttributes: { target: '_blank', rel: 'noopener noreferrer nofollow' } }),
  ],
  onUpdate: ({ editor }) => {
    emit('update:modelValue', editor.getHTML())
  },
})

// 외부(수정 모드 로딩 등)에서 modelValue가 바뀌면 동기화. 커서 리셋 루프 방지 가드.
watch(
  () => props.modelValue,
  (value) => {
    const current = editor.value?.getHTML()
    if (editor.value && value !== current) {
      editor.value.commands.setContent(value, false)
    }
  },
)

onBeforeUnmount(() => {
  editor.value?.destroy()
})

const FONT_SIZES: { label: string; value: string }[] = [
  { label: '작게', value: '13px' },
  { label: '보통', value: '16px' },
  { label: '크게', value: '20px' },
  { label: '아주 크게', value: '28px' },
]

function applyFontSize(event: Event): void {
  const value = (event.target as HTMLSelectElement).value
  if (!editor.value) return
  if (value) editor.value.chain().focus().setFontSize(value).run()
  else editor.value.chain().focus().unsetFontSize().run()
}

const FONT_FAMILIES: { label: string; value: string }[] = [
  { label: '명조', value: "'Nanum Myeongjo', serif" },
  { label: '고딕', value: "'Noto Sans KR', sans-serif" },
  { label: '나눔고딕', value: "'Nanum Gothic', sans-serif" },
  { label: '고운바탕', value: "'Gowun Batang', serif" },
  { label: '도현', value: "'Do Hyeon', sans-serif" },
  { label: '주아', value: "'Jua', sans-serif" },
  { label: '블랙한산스', value: "'Black Han Sans', sans-serif" },
  { label: '손글씨(펜)', value: "'Nanum Pen Script', cursive" },
  { label: '붓글씨', value: "'Nanum Brush Script', cursive" },
  { label: '둥근손글씨', value: "'Gaegu', cursive" },
  { label: '감자꽃', value: "'Gamja Flower', cursive" },
  { label: '하이멜로디', value: "'Hi Melody', cursive" },
  { label: '기랑해랑', value: "'Kirang Haerang', cursive" },
  { label: '연성', value: "'Yeon Sung', cursive" },
  { label: '독도', value: "'Dokdo', cursive" },
  { label: '푸어스토리', value: "'Poor Story', cursive" },
]

function applyFontFamily(event: Event): void {
  const value = (event.target as HTMLSelectElement).value
  if (!editor.value) return
  if (value) editor.value.chain().focus().setFontFamily(value).run()
  else editor.value.chain().focus().unsetFontFamily().run()
}

function applyColor(event: Event): void {
  const value = (event.target as HTMLInputElement).value
  editor.value?.chain().focus().setColor(value).run()
}

function setLink(): void {
  if (!editor.value) return
  const previous = (editor.value.getAttributes('link').href as string) ?? ''
  const url = window.prompt('링크 URL을 입력하세요 (비우면 해제)', previous)
  if (url === null) return
  if (url.trim() === '') {
    editor.value.chain().focus().extendMarkRange('link').unsetLink().run()
    return
  }
  editor.value.chain().focus().extendMarkRange('link').setLink({ href: url.trim() }).run()
}

const VIMEO_ID = /vimeo\.com\/(?:video\/)?(\d+)/i
// 유튜브 영상 ID 추출: watch?v=, youtu.be/, /embed/, /shorts/ 모두 지원.
const YOUTUBE_ID = /(?:youtube\.com\/(?:watch\?v=|embed\/|shorts\/)|youtu\.be\/)([\w-]{11})/i

// 입력 URL을 임베드 src로 변환. 지원하지 않는 주소면 null.
function toEmbedSrc(url: string): string | null {
  const yt = url.match(YOUTUBE_ID)
  if (yt) return `https://www.youtube-nocookie.com/embed/${yt[1]}`
  const vimeo = url.match(VIMEO_ID)
  if (vimeo) return `https://player.vimeo.com/video/${vimeo[1]}`
  return null
}

function addVideo(): void {
  if (!editor.value) return
  const url = window.prompt('유튜브 또는 비메오 영상 URL을 입력하세요')
  if (url === null || url.trim() === '') return
  const src = toEmbedSrc(url.trim())
  if (!src) {
    window.alert('유튜브 또는 비메오 영상 주소만 넣을 수 있어요.')
    return
  }
  // 일반 iframe 노드로 삽입 → 저장·수정 왕복에서 표식 의존 없이 보존된다.
  editor.value.chain().focus().insertContent({ type: 'iframe', attrs: { src } }).run()
}

// 음악 업로드: 파일 선택 → 서버 업로드 → audio 노드 삽입. 업로드 중 중복 클릭 방지.
const AUDIO_MAX_BYTES = 20 * 1024 * 1024
const audioInput = ref<HTMLInputElement | null>(null)
const audioUploading = ref(false)

function pickAudio(): void {
  if (audioUploading.value) return
  audioInput.value?.click()
}

async function onAudioSelected(event: Event): Promise<void> {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = '' // 같은 파일 재선택 허용
  if (!file || !editor.value) return
  if (file.size > AUDIO_MAX_BYTES) {
    window.alert('음악 파일은 20MB 이하만 올릴 수 있어요.')
    return
  }
  audioUploading.value = true
  try {
    const { url } = await uploadPostAudio(file)
    editor.value.chain().focus().insertContent({ type: 'audio', attrs: { src: url } }).run()
  } catch (e) {
    window.alert(e instanceof Error ? e.message : '음악 업로드에 실패했어요. 잠시 후 다시 시도해 주세요.')
  } finally {
    audioUploading.value = false
  }
}

function isActive(name: string, attrs?: Record<string, unknown>): boolean {
  if (attrs) return editor.value?.isActive(name, attrs) ?? false
  return editor.value?.isActive(name) ?? false
}

function isAlign(value: 'left' | 'center' | 'right'): boolean {
  return editor.value?.isActive({ textAlign: value }) ?? false
}
</script>

<template>
  <div class="rich-editor">
    <div v-if="editor" class="toolbar" role="toolbar" aria-label="서식 도구">
      <button type="button" class="tb-btn" :class="{ on: isActive('bold') }" title="굵게"
        @click="editor.chain().focus().toggleBold().run()"><strong>B</strong></button>
      <button type="button" class="tb-btn" :class="{ on: isActive('italic') }" title="기울임"
        @click="editor.chain().focus().toggleItalic().run()"><em>I</em></button>
      <button type="button" class="tb-btn" :class="{ on: isActive('underline') }" title="밑줄"
        @click="editor.chain().focus().toggleUnderline().run()"><u>U</u></button>
      <button type="button" class="tb-btn" :class="{ on: isActive('strike') }" title="취소선"
        @click="editor.chain().focus().toggleStrike().run()"><s>S</s></button>

      <span class="tb-sep" aria-hidden="true"></span>

      <button type="button" class="tb-btn" :class="{ on: isActive('heading', { level: 2 }) }" title="제목"
        @click="editor.chain().focus().toggleHeading({ level: 2 }).run()">H2</button>
      <button type="button" class="tb-btn" :class="{ on: isActive('heading', { level: 3 }) }" title="소제목"
        @click="editor.chain().focus().toggleHeading({ level: 3 }).run()">H3</button>

      <span class="tb-sep" aria-hidden="true"></span>

      <button type="button" class="tb-btn" :class="{ on: isActive('bulletList') }" title="글머리 목록"
        @click="editor.chain().focus().toggleBulletList().run()">• 목록</button>
      <button type="button" class="tb-btn" :class="{ on: isActive('orderedList') }" title="번호 목록"
        @click="editor.chain().focus().toggleOrderedList().run()">1. 목록</button>
      <button type="button" class="tb-btn" :class="{ on: isActive('blockquote') }" title="인용"
        @click="editor.chain().focus().toggleBlockquote().run()">＂ 인용</button>

      <span class="tb-sep" aria-hidden="true"></span>

      <select class="tb-select" title="글씨체" @change="applyFontFamily">
        <option value="">글씨체</option>
        <option v-for="f in FONT_FAMILIES" :key="f.value" :value="f.value" :style="{ fontFamily: f.value }">{{ f.label }}</option>
      </select>

      <select class="tb-select" title="글자 크기" @change="applyFontSize">
        <option value="">크기</option>
        <option v-for="f in FONT_SIZES" :key="f.value" :value="f.value">{{ f.label }}</option>
      </select>

      <label class="tb-color" title="글자색">
        <span aria-hidden="true">A</span>
        <input type="color" @input="applyColor" />
      </label>

      <span class="tb-sep" aria-hidden="true"></span>

      <button type="button" class="tb-btn" :class="{ on: isAlign('left') }" title="왼쪽 정렬"
        @click="editor.chain().focus().setTextAlign('left').run()">⬱</button>
      <button type="button" class="tb-btn" :class="{ on: isAlign('center') }" title="가운데 정렬"
        @click="editor.chain().focus().setTextAlign('center').run()">≡</button>
      <button type="button" class="tb-btn" :class="{ on: isAlign('right') }" title="오른쪽 정렬"
        @click="editor.chain().focus().setTextAlign('right').run()">⬲</button>

      <span class="tb-sep" aria-hidden="true"></span>

      <button type="button" class="tb-btn" :class="{ on: isActive('link') }" title="링크" @click="setLink">🔗 링크</button>
      <button type="button" class="tb-btn" title="영상 삽입" @click="addVideo">▶ 영상</button>
      <button type="button" class="tb-btn" title="음악 삽입(MP3·M4A·WAV·OGG, 20MB 이하)"
        :disabled="audioUploading" @click="pickAudio">{{ audioUploading ? '올리는 중…' : '🎵 음악' }}</button>
      <input ref="audioInput" type="file" accept="audio/mpeg,audio/mp4,audio/x-m4a,audio/wav,audio/x-wav,audio/ogg,.mp3,.m4a,.wav,.ogg"
        class="tb-file" @change="onAudioSelected" />
    </div>

    <EditorContent :editor="editor" class="editor-surface" />
  </div>
</template>

<style scoped>
.rich-editor {
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: rgba(6, 9, 18, 0.6);
  overflow: hidden;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.3rem;
  padding: 0.5rem 0.6rem;
  border-bottom: 1px solid var(--line);
  background: rgba(13, 27, 62, 0.4);
}
.tb-btn {
  min-width: 2rem;
  height: 2rem;
  padding: 0 0.5rem;
  font-size: 0.85rem;
  font-family: var(--sans);
  color: var(--ink-body);
  background: transparent;
  border: 1px solid transparent;
  border-radius: var(--radius);
  cursor: pointer;
  transition: color var(--dur) var(--ease), background var(--dur) var(--ease),
    border-color var(--dur) var(--ease);
  width: auto;
}
.tb-btn:hover {
  color: var(--ink-bright);
  border-color: var(--line);
}
.tb-btn.on {
  color: var(--bg-night);
  background: var(--gold-2);
  border-color: var(--gold-2);
}
.tb-sep {
  width: 1px;
  height: 1.4rem;
  margin: 0 0.2rem;
  background: var(--line);
}
.tb-select {
  height: 2rem;
  padding: 0 0.4rem;
  font-size: 0.82rem;
  color: var(--ink-body);
  background: rgba(6, 9, 18, 0.6);
  border: 1px solid var(--line);
  border-radius: var(--radius);
}
.tb-color {
  display: inline-flex;
  align-items: center;
  gap: 0.15rem;
  height: 2rem;
  padding: 0 0.4rem;
  color: var(--ink-body);
  border: 1px solid transparent;
  border-radius: var(--radius);
  cursor: pointer;
}
.tb-color:hover {
  border-color: var(--line);
}
.tb-color input[type='color'] {
  width: 1.4rem;
  height: 1.4rem;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
}
.editor-surface {
  padding: 0.4rem 0.2rem;
}

/* 편집 영역 타이포는 본문(.post-body)과 동일 톤 */
.editor-surface :deep(.ProseMirror) {
  min-height: 220px;
  padding: 0.6rem 0.9rem;
  line-height: 1.9;
  font-size: 1.05rem;
  color: var(--ink-bright);
  outline: none;
}
.editor-surface :deep(.ProseMirror:focus) {
  outline: none;
}
.editor-surface :deep(.ProseMirror p) {
  margin: 0 0 0.8em;
}
.editor-surface :deep(.ProseMirror h2) {
  font-size: 1.5rem;
  margin: 1.2em 0 0.5em;
}
.editor-surface :deep(.ProseMirror h3) {
  font-size: 1.25rem;
  margin: 1.1em 0 0.5em;
}
.editor-surface :deep(.ProseMirror ul),
.editor-surface :deep(.ProseMirror ol) {
  padding-left: 1.5em;
  margin: 0 0 0.8em;
}
.editor-surface :deep(.ProseMirror blockquote) {
  margin: 0 0 0.8em;
  padding-left: 1em;
  border-left: 3px solid var(--gold-1);
  color: var(--ink-body);
}
.editor-surface :deep(.ProseMirror a) {
  color: var(--gold-3);
  text-decoration: underline;
}
.editor-surface :deep(.ProseMirror img) {
  max-width: 100%;
  height: auto;
}
.editor-surface :deep(.ProseMirror iframe) {
  max-width: 100%;
  aspect-ratio: 16 / 9;
  border: 1px solid var(--line);
}
.editor-surface :deep(.ProseMirror audio) {
  display: block;
  width: 100%;
  margin: 0.6em 0;
}
.tb-file {
  display: none;
}
/* 빈 첫 문단에 placeholder 느낌(선택) */
.editor-surface :deep(.ProseMirror p.is-editor-empty:first-child::before) {
  content: attr(data-placeholder);
  color: var(--ink-faint);
  float: left;
  height: 0;
  pointer-events: none;
}
</style>
