import{F as e,L as t,M as n,N as r,O as i,P as a,k as o}from"./engagement-BUiKrtwx.js";import{A as s,D as c,E as l,F as u,H as d,J as f,N as p,R as m,T as h,U as g,Z as _,_ as v,g as y,k as b,p as x,u as S,w as C}from"./auth-B5u7Y81U.js";import{Ct as w,H as T,J as E,Q as D,St as O,V as ee,X as k,Y as A,Z as j,_t as M,at as N,bt as P,c as te,ct as ne,f as re,i as F,l as ie,lt as ae,mt as I,pt as oe,r as L,s as R,st as z,t as B,tt as V,u as se,ut as H,vt as U,xt as W}from"./Scrollbar-DJiy6esR.js";import{C as ce,b as G,o as le,v as ue}from"./fade-in-scale-up.cssr-zqqkC8zI.js";import{r as de}from"./Empty-BmrB-TOl.js";function K(e){if(typeof e==`number`)return{"":e.toString()};let t={};return e.split(/ +/).forEach(e=>{if(e===``)return;let[n,r]=e.split(`:`);r===void 0?t[``]=n:t[n]=r}),t}function q(e,t){if(e==null)return;let n=K(e);if(t===void 0)return n[``];if(typeof t==`string`)return n[t]??n[``];if(Array.isArray(t)){for(let e=t.length-1;e>=0;--e){let r=t[e];if(r in n)return n[r]}return n[``]}{let e,r=-1;return Object.keys(n).forEach(i=>{let a=Number(i);!Number.isNaN(a)&&t>=a&&a>=r&&(r=a,e=n[i])}),e}}var fe={xs:0,s:640,m:1024,l:1280,xl:1536,"2xl":1920};function pe(e){return`(min-width: ${e}px)`}var J={};function me(e=fe){if(!G||typeof window.matchMedia!=`function`)return v(()=>[]);let t=f({}),n=Object.keys(e),r=(e,n)=>{e.matches?t.value[n]=!0:t.value[n]=!1};return n.forEach(t=>{let n=e[t],i,a;J[n]===void 0?(i=window.matchMedia(pe(n)),i.addEventListener?i.addEventListener(`change`,e=>{a.forEach(n=>{n(e,t)})}):i.addListener&&i.addListener(e=>{a.forEach(n=>{n(e,t)})}),a=new Set,J[n]={mql:i,cbs:a}):(i=J[n].mql,a=J[n].cbs),a.add(r),i.matches&&a.forEach(e=>{e(i,t)})}),p(()=>{n.forEach(t=>{let{cbs:n}=J[e[t]];n.has(r)&&n.delete(r)})}),v(()=>{let{value:e}=t;return n.filter(t=>e[t])})}var he=new WeakSet;function Y(e){he.add(e)}function X(e){return!he.has(e)}function ge(e,t=`default`,n=[]){let r=e.$slots[t];return r===void 0?n:r()}function _e(e){let t=e.dirs?.find(({dir:e})=>e===S);return!!(t&&t.value===!1)}var ve=C({name:`ChevronDown`,render(){return l(`svg`,{viewBox:`0 0 16 16`,fill:`none`,xmlns:`http://www.w3.org/2000/svg`},l(`path`,{d:`M3.14645 5.64645C3.34171 5.45118 3.65829 5.45118 3.85355 5.64645L8 9.79289L12.1464 5.64645C12.3417 5.45118 12.6583 5.45118 12.8536 5.64645C13.0488 5.84171 13.0488 6.15829 12.8536 6.35355L8.35355 10.8536C8.15829 11.0488 7.84171 11.0488 7.64645 10.8536L3.14645 6.35355C2.95118 6.15829 2.95118 5.84171 3.14645 5.64645Z`,fill:`currentColor`}))}}),ye=r(`clear`,()=>l(`svg`,{viewBox:`0 0 16 16`,version:`1.1`,xmlns:`http://www.w3.org/2000/svg`},l(`g`,{stroke:`none`,"stroke-width":`1`,fill:`none`,"fill-rule":`evenodd`},l(`g`,{fill:`currentColor`,"fill-rule":`nonzero`},l(`path`,{d:`M8,2 C11.3137085,2 14,4.6862915 14,8 C14,11.3137085 11.3137085,14 8,14 C4.6862915,14 2,11.3137085 2,8 C2,4.6862915 4.6862915,2 8,2 Z M6.5343055,5.83859116 C6.33943736,5.70359511 6.07001296,5.72288026 5.89644661,5.89644661 L5.89644661,5.89644661 L5.83859116,5.9656945 C5.70359511,6.16056264 5.72288026,6.42998704 5.89644661,6.60355339 L5.89644661,6.60355339 L7.293,8 L5.89644661,9.39644661 L5.83859116,9.4656945 C5.70359511,9.66056264 5.72288026,9.92998704 5.89644661,10.1035534 L5.89644661,10.1035534 L5.9656945,10.1614088 C6.16056264,10.2964049 6.42998704,10.2771197 6.60355339,10.1035534 L6.60355339,10.1035534 L8,8.707 L9.39644661,10.1035534 L9.4656945,10.1614088 C9.66056264,10.2964049 9.92998704,10.2771197 10.1035534,10.1035534 L10.1035534,10.1035534 L10.1614088,10.0343055 C10.2964049,9.83943736 10.2771197,9.57001296 10.1035534,9.39644661 L10.1035534,9.39644661 L8.707,8 L10.1035534,6.60355339 L10.1614088,6.5343055 C10.2964049,6.33943736 10.2771197,6.07001296 10.1035534,5.89644661 L10.1035534,5.89644661 L10.0343055,5.83859116 C9.83943736,5.70359511 9.57001296,5.72288026 9.39644661,5.89644661 L9.39644661,5.89644661 L8,7.293 L6.60355339,5.89644661 Z`}))))),be=C({name:`Eye`,render(){return l(`svg`,{xmlns:`http://www.w3.org/2000/svg`,viewBox:`0 0 512 512`},l(`path`,{d:`M255.66 112c-77.94 0-157.89 45.11-220.83 135.33a16 16 0 0 0-.27 17.77C82.92 340.8 161.8 400 255.66 400c92.84 0 173.34-59.38 221.79-135.25a16.14 16.14 0 0 0 0-17.47C428.89 172.28 347.8 112 255.66 112z`,fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`}),l(`circle`,{cx:`256`,cy:`256`,r:`80`,fill:`none`,stroke:`currentColor`,"stroke-miterlimit":`10`,"stroke-width":`32`}))}}),xe=C({name:`EyeOff`,render(){return l(`svg`,{xmlns:`http://www.w3.org/2000/svg`,viewBox:`0 0 512 512`},l(`path`,{d:`M432 448a15.92 15.92 0 0 1-11.31-4.69l-352-352a16 16 0 0 1 22.62-22.62l352 352A16 16 0 0 1 432 448z`,fill:`currentColor`}),l(`path`,{d:`M255.66 384c-41.49 0-81.5-12.28-118.92-36.5c-34.07-22-64.74-53.51-88.7-91v-.08c19.94-28.57 41.78-52.73 65.24-72.21a2 2 0 0 0 .14-2.94L93.5 161.38a2 2 0 0 0-2.71-.12c-24.92 21-48.05 46.76-69.08 76.92a31.92 31.92 0 0 0-.64 35.54c26.41 41.33 60.4 76.14 98.28 100.65C162 402 207.9 416 255.66 416a239.13 239.13 0 0 0 75.8-12.58a2 2 0 0 0 .77-3.31l-21.58-21.58a4 4 0 0 0-3.83-1a204.8 204.8 0 0 1-51.16 6.47z`,fill:`currentColor`}),l(`path`,{d:`M490.84 238.6c-26.46-40.92-60.79-75.68-99.27-100.53C349 110.55 302 96 255.66 96a227.34 227.34 0 0 0-74.89 12.83a2 2 0 0 0-.75 3.31l21.55 21.55a4 4 0 0 0 3.88 1a192.82 192.82 0 0 1 50.21-6.69c40.69 0 80.58 12.43 118.55 37c34.71 22.4 65.74 53.88 89.76 91a.13.13 0 0 1 0 .16a310.72 310.72 0 0 1-64.12 72.73a2 2 0 0 0-.15 2.95l19.9 19.89a2 2 0 0 0 2.7.13a343.49 343.49 0 0 0 68.64-78.48a32.2 32.2 0 0 0-.1-34.78z`,fill:`currentColor`}),l(`path`,{d:`M256 160a95.88 95.88 0 0 0-21.37 2.4a2 2 0 0 0-1 3.38l112.59 112.56a2 2 0 0 0 3.38-1A96 96 0 0 0 256 160z`,fill:`currentColor`}),l(`path`,{d:`M165.78 233.66a2 2 0 0 0-3.38 1a96 96 0 0 0 115 115a2 2 0 0 0 1-3.38z`,fill:`currentColor`}))}}),Se=U(`base-clear`,`
 flex-shrink: 0;
 height: 1em;
 width: 1em;
 position: relative;
`,[M(`>`,[P(`clear`,`
 font-size: var(--n-clear-size);
 height: 1em;
 width: 1em;
 cursor: pointer;
 color: var(--n-clear-color);
 transition: color .3s var(--n-bezier);
 display: flex;
 `,[M(`&:hover`,`
 color: var(--n-clear-color-hover)!important;
 `),M(`&:active`,`
 color: var(--n-clear-color-pressed)!important;
 `)]),P(`placeholder`,`
 display: flex;
 `),P(`clear, placeholder`,`
 position: absolute;
 left: 50%;
 top: 50%;
 transform: translateX(-50%) translateY(-50%);
 `,[n({originalTransform:`translateX(-50%) translateY(-50%)`,left:`50%`,top:`50%`})])])]),Z=C({name:`BaseClear`,props:{clsPrefix:{type:String,required:!0},show:Boolean,onClear:Function},setup(e){return se(`-base-clear`,Se,_(e,`clsPrefix`)),{handleMouseDown(e){e.preventDefault()}}},render(){let{clsPrefix:e}=this;return l(`div`,{class:`${e}-base-clear`},l(a,null,{default:()=>{var t;return this.show?l(`div`,{key:`dismiss`,class:`${e}-base-clear__clear`,onClick:this.onClear,onMousedown:this.handleMouseDown,"data-clear":!0},E(this.$slots.icon,()=>[l(R,{clsPrefix:e},{default:()=>l(ye,null)})])):l(`div`,{key:`icon`,class:`${e}-base-clear__placeholder`},(t=this.$slots).placeholder?.call(t))}}))}}),Ce=C({name:`InternalSelectionSuffix`,props:{clsPrefix:{type:String,required:!0},showArrow:{type:Boolean,default:void 0},showClear:{type:Boolean,default:void 0},loading:{type:Boolean,default:!1},onClear:Function},setup(e,{slots:t}){return()=>{let{clsPrefix:n}=e;return l(o,{clsPrefix:n,class:`${n}-base-suffix`,strokeWidth:24,scale:.85,show:e.loading},{default:()=>e.showArrow?l(Z,{clsPrefix:n,show:e.showClear,onClear:e.onClear},{placeholder:()=>l(R,{clsPrefix:n,class:`${n}-base-suffix__arrow`},{default:()=>E(t.default,()=>[l(ve,null)])})}):null})}}}),we={paddingTiny:`0 8px`,paddingSmall:`0 10px`,paddingMedium:`0 12px`,paddingLarge:`0 14px`,clearSize:`16px`};function Te(e){let{textColor2:t,textColor3:n,textColorDisabled:r,primaryColor:i,primaryColorHover:a,inputColor:o,inputColorDisabled:s,borderColor:c,warningColor:l,warningColorHover:u,errorColor:d,errorColorHover:f,borderRadius:p,lineHeight:m,fontSizeTiny:h,fontSizeSmall:g,fontSizeMedium:_,fontSizeLarge:v,heightTiny:y,heightSmall:b,heightMedium:x,heightLarge:S,actionColor:C,clearColor:w,clearColorHover:T,clearColorPressed:E,placeholderColor:D,placeholderColorDisabled:O,iconColor:ee,iconColorDisabled:k,iconColorHover:A,iconColorPressed:j,fontWeight:M}=e;return Object.assign(Object.assign({},we),{fontWeight:M,countTextColorDisabled:r,countTextColor:n,heightTiny:y,heightSmall:b,heightMedium:x,heightLarge:S,fontSizeTiny:h,fontSizeSmall:g,fontSizeMedium:_,fontSizeLarge:v,lineHeight:m,lineHeightTextarea:m,borderRadius:p,iconSize:`16px`,groupLabelColor:C,groupLabelTextColor:t,textColor:t,textColorDisabled:r,textDecorationColor:t,caretColor:i,placeholderColor:D,placeholderColorDisabled:O,color:o,colorDisabled:s,colorFocus:o,groupLabelBorder:`1px solid ${c}`,border:`1px solid ${c}`,borderHover:`1px solid ${a}`,borderDisabled:`1px solid ${c}`,borderFocus:`1px solid ${a}`,boxShadowFocus:`0 0 0 2px ${H(i,{alpha:.2})}`,loadingColor:i,loadingColorWarning:l,borderWarning:`1px solid ${l}`,borderHoverWarning:`1px solid ${u}`,colorFocusWarning:o,borderFocusWarning:`1px solid ${u}`,boxShadowFocusWarning:`0 0 0 2px ${H(l,{alpha:.2})}`,caretColorWarning:l,loadingColorError:d,borderError:`1px solid ${d}`,borderHoverError:`1px solid ${f}`,colorFocusError:o,borderFocusError:`1px solid ${f}`,boxShadowFocusError:`0 0 0 2px ${H(d,{alpha:.2})}`,caretColorError:d,clearColor:w,clearColorHover:T,clearColorPressed:E,iconColor:ee,iconColorDisabled:k,iconColorHover:A,iconColorPressed:j,suffixTextColor:t})}var Ee=te({name:`Input`,common:F,peers:{Scrollbar:L},self:Te}),De=N(`n-input`),Oe=U(`input`,`
 max-width: 100%;
 cursor: text;
 line-height: 1.5;
 z-index: auto;
 outline: none;
 box-sizing: border-box;
 position: relative;
 display: inline-flex;
 border-radius: var(--n-border-radius);
 background-color: var(--n-color);
 transition: background-color .3s var(--n-bezier);
 font-size: var(--n-font-size);
 font-weight: var(--n-font-weight);
 --n-padding-vertical: calc((var(--n-height) - 1.5 * var(--n-font-size)) / 2);
`,[P(`input, textarea`,`
 overflow: hidden;
 flex-grow: 1;
 position: relative;
 `),P(`input-el, textarea-el, input-mirror, textarea-mirror, separator, placeholder`,`
 box-sizing: border-box;
 font-size: inherit;
 line-height: 1.5;
 font-family: inherit;
 border: none;
 outline: none;
 background-color: #0000;
 text-align: inherit;
 transition:
 -webkit-text-fill-color .3s var(--n-bezier),
 caret-color .3s var(--n-bezier),
 color .3s var(--n-bezier),
 text-decoration-color .3s var(--n-bezier);
 `),P(`input-el, textarea-el`,`
 -webkit-appearance: none;
 scrollbar-width: none;
 width: 100%;
 min-width: 0;
 text-decoration-color: var(--n-text-decoration-color);
 color: var(--n-text-color);
 caret-color: var(--n-caret-color);
 background-color: transparent;
 `,[M(`&::-webkit-scrollbar, &::-webkit-scrollbar-track-piece, &::-webkit-scrollbar-thumb`,`
 width: 0;
 height: 0;
 display: none;
 `),M(`&::placeholder`,`
 color: #0000;
 -webkit-text-fill-color: transparent !important;
 `),M(`&:-webkit-autofill ~`,[P(`placeholder`,`display: none;`)])]),W(`round`,[O(`textarea`,`border-radius: calc(var(--n-height) / 2);`)]),P(`placeholder`,`
 pointer-events: none;
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 overflow: hidden;
 color: var(--n-placeholder-color);
 `,[M(`span`,`
 width: 100%;
 display: inline-block;
 `)]),W(`textarea`,[P(`placeholder`,`overflow: visible;`)]),O(`autosize`,`width: 100%;`),W(`autosize`,[P(`textarea-el, input-el`,`
 position: absolute;
 top: 0;
 left: 0;
 height: 100%;
 `)]),U(`input-wrapper`,`
 overflow: hidden;
 display: inline-flex;
 flex-grow: 1;
 position: relative;
 padding-left: var(--n-padding-left);
 padding-right: var(--n-padding-right);
 `),P(`input-mirror`,`
 padding: 0;
 height: var(--n-height);
 line-height: var(--n-height);
 overflow: hidden;
 visibility: hidden;
 position: static;
 white-space: pre;
 pointer-events: none;
 `),P(`input-el`,`
 padding: 0;
 height: var(--n-height);
 line-height: var(--n-height);
 `,[M(`&[type=password]::-ms-reveal`,`display: none;`),M(`+`,[P(`placeholder`,`
 display: flex;
 align-items: center; 
 `)])]),O(`textarea`,[P(`placeholder`,`white-space: nowrap;`)]),P(`eye`,`
 display: flex;
 align-items: center;
 justify-content: center;
 transition: color .3s var(--n-bezier);
 `),W(`textarea`,`width: 100%;`,[U(`input-word-count`,`
 position: absolute;
 right: var(--n-padding-right);
 bottom: var(--n-padding-vertical);
 `),W(`resizable`,[U(`input-wrapper`,`
 resize: vertical;
 min-height: var(--n-height);
 `)]),P(`textarea-el, textarea-mirror, placeholder`,`
 height: 100%;
 padding-left: 0;
 padding-right: 0;
 padding-top: var(--n-padding-vertical);
 padding-bottom: var(--n-padding-vertical);
 word-break: break-word;
 display: inline-block;
 vertical-align: bottom;
 box-sizing: border-box;
 line-height: var(--n-line-height-textarea);
 margin: 0;
 resize: none;
 white-space: pre-wrap;
 scroll-padding-block-end: var(--n-padding-vertical);
 `),P(`textarea-mirror`,`
 width: 100%;
 pointer-events: none;
 overflow: hidden;
 visibility: hidden;
 position: static;
 white-space: pre-wrap;
 overflow-wrap: break-word;
 `)]),W(`pair`,[P(`input-el, placeholder`,`text-align: center;`),P(`separator`,`
 display: flex;
 align-items: center;
 transition: color .3s var(--n-bezier);
 color: var(--n-text-color);
 white-space: nowrap;
 `,[U(`icon`,`
 color: var(--n-icon-color);
 `),U(`base-icon`,`
 color: var(--n-icon-color);
 `)])]),W(`disabled`,`
 cursor: not-allowed;
 background-color: var(--n-color-disabled);
 `,[P(`border`,`border: var(--n-border-disabled);`),P(`input-el, textarea-el`,`
 cursor: not-allowed;
 color: var(--n-text-color-disabled);
 text-decoration-color: var(--n-text-color-disabled);
 `),P(`placeholder`,`color: var(--n-placeholder-color-disabled);`),P(`separator`,`color: var(--n-text-color-disabled);`,[U(`icon`,`
 color: var(--n-icon-color-disabled);
 `),U(`base-icon`,`
 color: var(--n-icon-color-disabled);
 `)]),U(`input-word-count`,`
 color: var(--n-count-text-color-disabled);
 `),P(`suffix, prefix`,`color: var(--n-text-color-disabled);`,[U(`icon`,`
 color: var(--n-icon-color-disabled);
 `),U(`internal-icon`,`
 color: var(--n-icon-color-disabled);
 `)])]),O(`disabled`,[P(`eye`,`
 color: var(--n-icon-color);
 cursor: pointer;
 `,[M(`&:hover`,`
 color: var(--n-icon-color-hover);
 `),M(`&:active`,`
 color: var(--n-icon-color-pressed);
 `)]),M(`&:hover`,[P(`state-border`,`border: var(--n-border-hover);`)]),W(`focus`,`background-color: var(--n-color-focus);`,[P(`state-border`,`
 border: var(--n-border-focus);
 box-shadow: var(--n-box-shadow-focus);
 `)])]),P(`border, state-border`,`
 box-sizing: border-box;
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 pointer-events: none;
 border-radius: inherit;
 border: var(--n-border);
 transition:
 box-shadow .3s var(--n-bezier),
 border-color .3s var(--n-bezier);
 `),P(`state-border`,`
 border-color: #0000;
 z-index: 1;
 `),P(`prefix`,`margin-right: 4px;`),P(`suffix`,`
 margin-left: 4px;
 `),P(`suffix, prefix`,`
 transition: color .3s var(--n-bezier);
 flex-wrap: nowrap;
 flex-shrink: 0;
 line-height: var(--n-height);
 white-space: nowrap;
 display: inline-flex;
 align-items: center;
 justify-content: center;
 color: var(--n-suffix-text-color);
 `,[U(`base-loading`,`
 font-size: var(--n-icon-size);
 margin: 0 2px;
 color: var(--n-loading-color);
 `),U(`base-clear`,`
 font-size: var(--n-icon-size);
 `,[P(`placeholder`,[U(`base-icon`,`
 transition: color .3s var(--n-bezier);
 color: var(--n-icon-color);
 font-size: var(--n-icon-size);
 `)])]),M(`>`,[U(`icon`,`
 transition: color .3s var(--n-bezier);
 color: var(--n-icon-color);
 font-size: var(--n-icon-size);
 `)]),U(`base-icon`,`
 font-size: var(--n-icon-size);
 `)]),U(`input-word-count`,`
 pointer-events: none;
 line-height: 1.5;
 font-size: .85em;
 color: var(--n-count-text-color);
 transition: color .3s var(--n-bezier);
 margin-left: 4px;
 font-variant: tabular-nums;
 `),[`warning`,`error`].map(e=>W(`${e}-status`,[O(`disabled`,[U(`base-loading`,`
 color: var(--n-loading-color-${e})
 `),P(`input-el, textarea-el`,`
 caret-color: var(--n-caret-color-${e});
 `),P(`state-border`,`
 border: var(--n-border-${e});
 `),M(`&:hover`,[P(`state-border`,`
 border: var(--n-border-hover-${e});
 `)]),M(`&:focus`,`
 background-color: var(--n-color-focus-${e});
 `,[P(`state-border`,`
 box-shadow: var(--n-box-shadow-focus-${e});
 border: var(--n-border-focus-${e});
 `)]),W(`focus`,`
 background-color: var(--n-color-focus-${e});
 `,[P(`state-border`,`
 box-shadow: var(--n-box-shadow-focus-${e});
 border: var(--n-border-focus-${e});
 `)])])]))]),ke=U(`input`,[W(`disabled`,[P(`input-el, textarea-el`,`
 -webkit-text-fill-color: var(--n-text-color-disabled);
 `)])]);function Ae(e){let t=0;for(let n of e)t++;return t}function je(e){return e===``||e==null}function Me(e){let t=f(null);function n(){let{value:n}=e;if(!n?.focus){i();return}let{selectionStart:r,selectionEnd:a,value:o}=n;if(r==null||a==null){i();return}t.value={start:r,end:a,beforeText:o.slice(0,r),afterText:o.slice(a)}}function r(){var n;let{value:r}=t,{value:i}=e;if(!r||!i)return;let{value:a}=i,{start:o,beforeText:s,afterText:c}=r,l=a.length;if(a.endsWith(c))l=a.length-c.length;else if(a.startsWith(s))l=s.length;else{let e=s[o-1],t=a.indexOf(e,o-1);t!==-1&&(l=t+1)}(n=i.setSelectionRange)==null||n.call(i,l,l)}function i(){t.value=null}return d(e,i),{recordCursor:n,restoreCursor:r}}var Q=C({name:`InputWordCount`,setup(e,{slots:t}){let{mergedValueRef:n,maxlengthRef:r,mergedClsPrefixRef:i,countGraphemesRef:a}=c(De),o=v(()=>{let{value:e}=n;return e===null||Array.isArray(e)?0:(a.value||Ae)(e)});return()=>{let{value:e}=r,{value:a}=n;return l(`span`,{class:`${i.value}-input-word-count`},A(t.default,{value:a===null||Array.isArray(a)?``:a},()=>[e===void 0?o.value:`${o.value} / ${e}`]))}}}),Ne=Object.assign(Object.assign({},ie.props),{bordered:{type:Boolean,default:void 0},type:{type:String,default:`text`},placeholder:[Array,String],defaultValue:{type:[String,Array],default:null},value:[String,Array],disabled:{type:Boolean,default:void 0},size:String,rows:{type:[Number,String],default:3},round:Boolean,minlength:[String,Number],maxlength:[String,Number],clearable:Boolean,autosize:{type:[Boolean,Object],default:!1},pair:Boolean,separator:String,readonly:{type:[String,Boolean],default:!1},passivelyActivated:Boolean,showPasswordOn:String,stateful:{type:Boolean,default:!0},autofocus:Boolean,inputProps:Object,resizable:{type:Boolean,default:!0},showCount:Boolean,loading:{type:Boolean,default:void 0},allowInput:Function,renderCount:Function,onMousedown:Function,onKeydown:Function,onKeyup:[Function,Array],onInput:[Function,Array],onFocus:[Function,Array],onBlur:[Function,Array],onClick:[Function,Array],onChange:[Function,Array],onClear:[Function,Array],countGraphemes:Function,status:String,"onUpdate:value":[Function,Array],onUpdateValue:[Function,Array],textDecoration:[String,Array],attrSize:{type:Number,default:20},onInputBlur:[Function,Array],onInputFocus:[Function,Array],onDeactivate:[Function,Array],onActivate:[Function,Array],onWrapperFocus:[Function,Array],onWrapperBlur:[Function,Array],internalDeactivateOnEnter:Boolean,internalForceFocus:Boolean,internalLoadingBeforeSuffix:{type:Boolean,default:!0},showPasswordToggle:Boolean}),Pe=C({name:`Input`,props:Ne,slots:Object,setup(t){let{mergedClsPrefixRef:n,mergedBorderedRef:r,inlineThemeDisabled:a,mergedRtlRef:o,mergedComponentPropsRef:c}=T(t),l=ie(`Input`,`-input`,Oe,Ee,t,n);i&&se(`-input-safari`,ke,n);let p=f(null),y=f(null),b=f(null),x=f(null),S=f(null),C=f(null),E=f(null),O=Me(E),k=f(null),{localeRef:A}=de(`Input`),j=f(t.defaultValue),M=_(t,`value`),N=ue(M,j),P=e(t,{mergedSize:e=>{let{size:n}=t;if(n)return n;let{mergedSize:r}=e||{};return r?.value?r.value:c?.value?.Input?.size||`medium`}}),{mergedSizeRef:te,mergedDisabledRef:F,mergedStatusRef:I}=P,L=f(!1),R=f(!1),B=f(!1),V=f(!1),H=null,U=v(()=>{let{placeholder:e,pair:n}=t;return n?Array.isArray(e)?e:e===void 0?[``,``]:[e,e]:e===void 0?[A.value.placeholder]:[e]}),W=v(()=>{let{value:e}=B,{value:t}=N,{value:n}=U;return!e&&(je(t)||Array.isArray(t)&&je(t[0]))&&n[0]}),ce=v(()=>{let{value:e}=B,{value:t}=N,{value:n}=U;return!e&&n[1]&&(je(t)||Array.isArray(t)&&je(t[1]))}),G=z(()=>t.internalForceFocus||L.value),le=z(()=>{if(F.value||t.readonly||!t.clearable||!G.value&&!R.value)return!1;let{value:e}=N,{value:n}=G;return t.pair?!!(Array.isArray(e)&&(e[0]||e[1]))&&(R.value||n):!!e&&(R.value||n)}),K=v(()=>{let{showPasswordOn:e}=t;if(e)return e;if(t.showPasswordToggle)return`click`}),q=f(!1),fe=v(()=>{let{textDecoration:e}=t;return e?Array.isArray(e)?e.map(e=>({textDecoration:e})):[{textDecoration:e}]:[``,``]}),pe=f(void 0),J=()=>{if(t.type===`textarea`){let{autosize:e}=t;if(e&&(pe.value=k.value?.$el?.offsetWidth),!y.value||typeof e==`boolean`)return;let{paddingTop:n,paddingBottom:r,lineHeight:i}=window.getComputedStyle(y.value),a=Number(n.slice(0,-2)),o=Number(r.slice(0,-2)),s=Number(i.slice(0,-2)),{value:c}=b;if(!c)return;if(e.minRows){let t=Math.max(e.minRows,1),n=`${a+o+s*t}px`;c.style.minHeight=n}if(e.maxRows){let t=`${a+o+s*e.maxRows}px`;c.style.maxHeight=t}}},me=v(()=>{let{maxlength:e}=t;return e===void 0?void 0:Number(e)});u(()=>{let{value:e}=N;Array.isArray(e)||rt(e)});let he=h().proxy;function Y(e,n){let{onUpdateValue:r,"onUpdate:value":i,onInput:a}=t,{nTriggerFormInput:o}=P;r&&D(r,e,n),i&&D(i,e,n),a&&D(a,e,n),j.value=e,o()}function X(e,n){let{onChange:r}=t,{nTriggerFormChange:i}=P;r&&D(r,e,n),j.value=e,i()}function ge(e){let{onBlur:n}=t,{nTriggerFormBlur:r}=P;n&&D(n,e),r()}function _e(e){let{onFocus:n}=t,{nTriggerFormFocus:r}=P;n&&D(n,e),r()}function ve(e){let{onClear:n}=t;n&&D(n,e)}function ye(e){let{onInputBlur:n}=t;n&&D(n,e)}function be(e){let{onInputFocus:n}=t;n&&D(n,e)}function xe(){let{onDeactivate:e}=t;e&&D(e)}function Se(){let{onActivate:e}=t;e&&D(e)}function Z(e){let{onClick:n}=t;n&&D(n,e)}function Ce(e){let{onWrapperFocus:n}=t;n&&D(n,e)}function we(e){let{onWrapperBlur:n}=t;n&&D(n,e)}function Te(){B.value=!0}function Ae(e){B.value=!1,e.target===C.value?Q(e,1):Q(e,0)}function Q(e,n=0,r=`input`){let i=e.target.value;if(rt(i),e instanceof InputEvent&&!e.isComposing&&(B.value=!1),t.type===`textarea`){let{value:e}=k;e&&e.syncUnifiedContainer()}if(H=i,B.value)return;O.recordCursor();let a=Ne(i);if(a)if(!t.pair)r===`input`?Y(i,{source:n}):X(i,{source:n});else{let{value:e}=N;e=Array.isArray(e)?[e[0],e[1]]:[``,``],e[n]=i,r===`input`?Y(e,{source:n}):X(e,{source:n})}he.$forceUpdate(),a||s(O.restoreCursor)}function Ne(e){let{countGraphemes:n,maxlength:r,minlength:i}=t;if(n){let t;if(r!==void 0&&(t===void 0&&(t=n(e)),t>Number(r))||i!==void 0&&(t===void 0&&(t=n(e)),t<Number(r)))return!1}let{allowInput:a}=t;return typeof a!=`function`||a(e)}function Pe(e){ye(e),e.relatedTarget===p.value&&xe(),(e.relatedTarget===null||e.relatedTarget!==S.value&&e.relatedTarget!==C.value&&e.relatedTarget!==y.value)&&(V.value=!1),$(e,`blur`),E.value=null}function Fe(e,t){be(e),L.value=!0,V.value=!0,Se(),$(e,`focus`),t===0?E.value=S.value:t===1?E.value=C.value:t===2&&(E.value=y.value)}function Ie(e){t.passivelyActivated&&(we(e),$(e,`blur`))}function Le(e){t.passivelyActivated&&(L.value=!0,Ce(e),$(e,`focus`))}function $(e,t){e.relatedTarget!==null&&(e.relatedTarget===S.value||e.relatedTarget===C.value||e.relatedTarget===y.value||e.relatedTarget===p.value)||(t===`focus`?(_e(e),L.value=!0):t===`blur`&&(ge(e),L.value=!1))}function Re(e,t){Q(e,t,`change`)}function ze(e){Z(e)}function Be(e){ve(e),Ve()}function Ve(){t.pair?(Y([``,``],{source:`clear`}),X([``,``],{source:`clear`})):(Y(``,{source:`clear`}),X(``,{source:`clear`}))}function He(e){let{onMousedown:n}=t;n&&n(e);let{tagName:r}=e.target;if(r!==`INPUT`&&r!==`TEXTAREA`){if(t.resizable){let{value:t}=p;if(t){let{left:n,top:r,width:i,height:a}=t.getBoundingClientRect();if(n+i-14<e.clientX&&e.clientX<n+i&&r+a-14<e.clientY&&e.clientY<r+a)return}}e.preventDefault(),L.value||Ze()}}function Ue(){var e;R.value=!0,t.type===`textarea`&&((e=k.value)==null||e.handleMouseEnterWrapper())}function We(){var e;R.value=!1,t.type===`textarea`&&((e=k.value)==null||e.handleMouseLeaveWrapper())}function Ge(){F.value||K.value===`click`&&(q.value=!q.value)}function Ke(e){if(F.value)return;e.preventDefault();let t=e=>{e.preventDefault(),ne(`mouseup`,document,t)};if(ae(`mouseup`,document,t),K.value!==`mousedown`)return;q.value=!0;let n=()=>{q.value=!1,ne(`mouseup`,document,n)};ae(`mouseup`,document,n)}function qe(e){t.onKeyup&&D(t.onKeyup,e)}function Je(e){switch(t.onKeydown&&D(t.onKeydown,e),e.key){case`Escape`:Xe();break;case`Enter`:Ye(e)}}function Ye(e){var n,r;if(t.passivelyActivated){let{value:i}=V;if(i){t.internalDeactivateOnEnter&&Xe();return}e.preventDefault(),t.type===`textarea`?(n=y.value)==null||n.focus():(r=S.value)==null||r.focus()}}function Xe(){t.passivelyActivated&&(V.value=!1,s(()=>{var e;(e=p.value)==null||e.focus()}))}function Ze(){var e,n,r;F.value||(t.passivelyActivated?(e=p.value)==null||e.focus():((n=y.value)==null||n.focus(),(r=S.value)==null||r.focus()))}function Qe(){p.value?.contains(document.activeElement)&&document.activeElement.blur()}function $e(){var e,t;(e=y.value)==null||e.select(),(t=S.value)==null||t.select()}function et(){F.value||(y.value?y.value.focus():S.value&&S.value.focus())}function tt(){let{value:e}=p;e?.contains(document.activeElement)&&e!==document.activeElement&&Xe()}function nt(e){if(t.type===`textarea`){let{value:t}=y;t?.scrollTo(e)}else{let{value:t}=S;t?.scrollTo(e)}}function rt(e){let{type:n,pair:r,autosize:i}=t;if(!r&&i)if(n===`textarea`){let{value:t}=b;t&&(t.textContent=`${e??``}\r\n`)}else{let{value:t}=x;t&&(e?t.textContent=e:t.innerHTML=`&nbsp;`)}}function it(){J()}let at=f({top:`0`});function ot(e){var t;let{scrollTop:n}=e.target;at.value.top=`${-n}px`,(t=k.value)==null||t.syncUnifiedContainer()}let st=null;g(()=>{let{autosize:e,type:n}=t;e&&n===`textarea`?st=d(N,e=>{!Array.isArray(e)&&e!==H&&rt(e)}):st?.()});let ct=null;g(()=>{t.type===`textarea`?ct=d(N,e=>{var t;!Array.isArray(e)&&e!==H&&((t=k.value)==null||t.syncUnifiedContainer())}):ct?.()}),m(De,{mergedValueRef:N,maxlengthRef:me,mergedClsPrefixRef:n,countGraphemesRef:_(t,`countGraphemes`)});let lt={wrapperElRef:p,inputElRef:S,textareaElRef:y,isCompositing:B,clear:Ve,focus:Ze,blur:Qe,select:$e,deactivate:tt,activate:et,scrollTo:nt},ut=re(`Input`,o,n),dt=v(()=>{let{value:e}=te,{common:{cubicBezierEaseInOut:t},self:{color:n,borderRadius:r,textColor:i,caretColor:a,caretColorError:o,caretColorWarning:s,textDecorationColor:c,border:u,borderDisabled:d,borderHover:f,borderFocus:p,placeholderColor:m,placeholderColorDisabled:h,lineHeightTextarea:g,colorDisabled:_,colorFocus:v,textColorDisabled:y,boxShadowFocus:b,iconSize:x,colorFocusWarning:S,boxShadowFocusWarning:C,borderWarning:T,borderFocusWarning:E,borderHoverWarning:D,colorFocusError:O,boxShadowFocusError:ee,borderError:k,borderFocusError:A,borderHoverError:j,clearSize:M,clearColor:N,clearColorHover:P,clearColorPressed:ne,iconColor:re,iconColorDisabled:F,suffixTextColor:ie,countTextColor:ae,countTextColorDisabled:I,iconColorHover:L,iconColorPressed:R,loadingColor:z,loadingColorError:B,loadingColorWarning:V,fontWeight:se,[w(`padding`,e)]:H,[w(`fontSize`,e)]:U,[w(`height`,e)]:W}}=l.value,{left:ce,right:G}=oe(H);return{"--n-bezier":t,"--n-count-text-color":ae,"--n-count-text-color-disabled":I,"--n-color":n,"--n-font-size":U,"--n-font-weight":se,"--n-border-radius":r,"--n-height":W,"--n-padding-left":ce,"--n-padding-right":G,"--n-text-color":i,"--n-caret-color":a,"--n-text-decoration-color":c,"--n-border":u,"--n-border-disabled":d,"--n-border-hover":f,"--n-border-focus":p,"--n-placeholder-color":m,"--n-placeholder-color-disabled":h,"--n-icon-size":x,"--n-line-height-textarea":g,"--n-color-disabled":_,"--n-color-focus":v,"--n-text-color-disabled":y,"--n-box-shadow-focus":b,"--n-loading-color":z,"--n-caret-color-warning":s,"--n-color-focus-warning":S,"--n-box-shadow-focus-warning":C,"--n-border-warning":T,"--n-border-focus-warning":E,"--n-border-hover-warning":D,"--n-loading-color-warning":V,"--n-caret-color-error":o,"--n-color-focus-error":O,"--n-box-shadow-focus-error":ee,"--n-border-error":k,"--n-border-focus-error":A,"--n-border-hover-error":j,"--n-loading-color-error":B,"--n-clear-color":N,"--n-clear-size":M,"--n-clear-color-hover":P,"--n-clear-color-pressed":ne,"--n-icon-color":re,"--n-icon-color-hover":L,"--n-icon-color-pressed":R,"--n-icon-color-disabled":F,"--n-suffix-text-color":ie}}),ft=a?ee(`input`,v(()=>{let{value:e}=te;return e[0]}),dt,t):void 0;return Object.assign(Object.assign({},lt),{wrapperElRef:p,inputElRef:S,inputMirrorElRef:x,inputEl2Ref:C,textareaElRef:y,textareaMirrorElRef:b,textareaScrollbarInstRef:k,rtlEnabled:ut,uncontrolledValue:j,mergedValue:N,passwordVisible:q,mergedPlaceholder:U,showPlaceholder1:W,showPlaceholder2:ce,mergedFocus:G,isComposing:B,activated:V,showClearButton:le,mergedSize:te,mergedDisabled:F,textDecorationStyle:fe,mergedClsPrefix:n,mergedBordered:r,mergedShowPasswordOn:K,placeholderStyle:at,mergedStatus:I,textAreaScrollContainerWidth:pe,handleTextAreaScroll:ot,handleCompositionStart:Te,handleCompositionEnd:Ae,handleInput:Q,handleInputBlur:Pe,handleInputFocus:Fe,handleWrapperBlur:Ie,handleWrapperFocus:Le,handleMouseEnter:Ue,handleMouseLeave:We,handleMouseDown:He,handleChange:Re,handleClick:ze,handleClear:Be,handlePasswordToggleClick:Ge,handlePasswordToggleMousedown:Ke,handleWrapperKeydown:Je,handleWrapperKeyup:qe,handleTextAreaMirrorResize:it,getTextareaScrollContainer:()=>y.value,mergedTheme:l,cssVars:a?void 0:dt,themeClass:ft?.themeClass,onRender:ft?.onRender})},render(){let{mergedClsPrefix:e,mergedStatus:t,themeClass:n,type:r,countGraphemes:i,onRender:a}=this,o=this.$slots;return a?.(),l(`div`,{ref:`wrapperElRef`,class:[`${e}-input`,`${e}-input--${this.mergedSize}-size`,n,t&&`${e}-input--${t}-status`,{[`${e}-input--rtl`]:this.rtlEnabled,[`${e}-input--disabled`]:this.mergedDisabled,[`${e}-input--textarea`]:r===`textarea`,[`${e}-input--resizable`]:this.resizable&&!this.autosize,[`${e}-input--autosize`]:this.autosize,[`${e}-input--round`]:this.round&&r!==`textarea`,[`${e}-input--pair`]:this.pair,[`${e}-input--focus`]:this.mergedFocus,[`${e}-input--stateful`]:this.stateful}],style:this.cssVars,tabindex:!this.mergedDisabled&&this.passivelyActivated&&!this.activated?0:void 0,onFocus:this.handleWrapperFocus,onBlur:this.handleWrapperBlur,onClick:this.handleClick,onMousedown:this.handleMouseDown,onMouseenter:this.handleMouseEnter,onMouseleave:this.handleMouseLeave,onCompositionstart:this.handleCompositionStart,onCompositionend:this.handleCompositionEnd,onKeyup:this.handleWrapperKeyup,onKeydown:this.handleWrapperKeydown},l(`div`,{class:`${e}-input-wrapper`},k(o.prefix,t=>t&&l(`div`,{class:`${e}-input__prefix`},t)),r===`textarea`?l(B,{ref:`textareaScrollbarInstRef`,class:`${e}-input__textarea`,container:this.getTextareaScrollContainer,theme:this.theme?.peers?.Scrollbar,themeOverrides:this.themeOverrides?.peers?.Scrollbar,triggerDisplayManually:!0,useUnifiedContainer:!0,internalHoistYRail:!0},{default:()=>{let{textAreaScrollContainerWidth:t}=this,n={width:this.autosize&&t&&`${t}px`};return l(x,null,l(`textarea`,Object.assign({},this.inputProps,{ref:`textareaElRef`,class:[`${e}-input__textarea-el`,this.inputProps?.class],autofocus:this.autofocus,rows:Number(this.rows),placeholder:this.placeholder,value:this.mergedValue,disabled:this.mergedDisabled,maxlength:i?void 0:this.maxlength,minlength:i?void 0:this.minlength,readonly:this.readonly,tabindex:this.passivelyActivated&&!this.activated?-1:void 0,style:[this.textDecorationStyle[0],this.inputProps?.style,n],onBlur:this.handleInputBlur,onFocus:e=>{this.handleInputFocus(e,2)},onInput:this.handleInput,onChange:this.handleChange,onScroll:this.handleTextAreaScroll})),this.showPlaceholder1?l(`div`,{class:`${e}-input__placeholder`,style:[this.placeholderStyle,n],key:`placeholder`},this.mergedPlaceholder[0]):null,this.autosize?l(V,{onResize:this.handleTextAreaMirrorResize},{default:()=>l(`div`,{ref:`textareaMirrorElRef`,class:`${e}-input__textarea-mirror`,key:`mirror`})}):null)}}):l(`div`,{class:`${e}-input__input`},l(`input`,Object.assign({type:r===`password`&&this.mergedShowPasswordOn&&this.passwordVisible?`text`:r},this.inputProps,{ref:`inputElRef`,class:[`${e}-input__input-el`,this.inputProps?.class],style:[this.textDecorationStyle[0],this.inputProps?.style],tabindex:this.passivelyActivated&&!this.activated?-1:this.inputProps?.tabindex,placeholder:this.mergedPlaceholder[0],disabled:this.mergedDisabled,maxlength:i?void 0:this.maxlength,minlength:i?void 0:this.minlength,value:Array.isArray(this.mergedValue)?this.mergedValue[0]:this.mergedValue,readonly:this.readonly,autofocus:this.autofocus,size:this.attrSize,onBlur:this.handleInputBlur,onFocus:e=>{this.handleInputFocus(e,0)},onInput:e=>{this.handleInput(e,0)},onChange:e=>{this.handleChange(e,0)}})),this.showPlaceholder1?l(`div`,{class:`${e}-input__placeholder`},l(`span`,null,this.mergedPlaceholder[0])):null,this.autosize?l(`div`,{class:`${e}-input__input-mirror`,key:`mirror`,ref:`inputMirrorElRef`},`\xA0`):null),!this.pair&&k(o.suffix,t=>t||this.clearable||this.showCount||this.mergedShowPasswordOn||this.loading!==void 0?l(`div`,{class:`${e}-input__suffix`},[k(o[`clear-icon-placeholder`],t=>(this.clearable||t)&&l(Z,{clsPrefix:e,show:this.showClearButton,onClear:this.handleClear},{placeholder:()=>t,icon:()=>{var e;return(e=this.$slots)[`clear-icon`]?.call(e)}})),this.internalLoadingBeforeSuffix?null:t,this.loading===void 0?null:l(Ce,{clsPrefix:e,loading:this.loading,showArrow:!1,showClear:!1,style:this.cssVars}),this.internalLoadingBeforeSuffix?t:null,this.showCount&&this.type!==`textarea`?l(Q,null,{default:e=>{let{renderCount:t}=this;return t?t(e):o.count?.call(o,e)}}):null,this.mergedShowPasswordOn&&this.type===`password`?l(`div`,{class:`${e}-input__eye`,onMousedown:this.handlePasswordToggleMousedown,onClick:this.handlePasswordToggleClick},this.passwordVisible?E(o[`password-visible-icon`],()=>[l(R,{clsPrefix:e},{default:()=>l(be,null)})]):E(o[`password-invisible-icon`],()=>[l(R,{clsPrefix:e},{default:()=>l(xe,null)})])):null]):null)),this.pair?l(`span`,{class:`${e}-input__separator`},E(o.separator,()=>[this.separator])):null,this.pair?l(`div`,{class:`${e}-input-wrapper`},l(`div`,{class:`${e}-input__input`},l(`input`,{ref:`inputEl2Ref`,type:this.type,class:`${e}-input__input-el`,tabindex:this.passivelyActivated&&!this.activated?-1:void 0,placeholder:this.mergedPlaceholder[1],disabled:this.mergedDisabled,maxlength:i?void 0:this.maxlength,minlength:i?void 0:this.minlength,value:Array.isArray(this.mergedValue)?this.mergedValue[1]:void 0,readonly:this.readonly,style:this.textDecorationStyle[1],onBlur:this.handleInputBlur,onFocus:e=>{this.handleInputFocus(e,1)},onInput:e=>{this.handleInput(e,1)},onChange:e=>{this.handleChange(e,1)}}),this.showPlaceholder2?l(`div`,{class:`${e}-input__placeholder`},l(`span`,null,this.mergedPlaceholder[1])):null),k(o.suffix,t=>(this.clearable||t)&&l(`div`,{class:`${e}-input__suffix`},[this.clearable&&l(Z,{clsPrefix:e,show:this.showClearButton,onClear:this.handleClear},{icon:()=>o[`clear-icon`]?.call(o),placeholder:()=>o[`clear-icon-placeholder`]?.call(o)}),t]))):null,this.mergedBordered?l(`div`,{class:`${e}-input__border`}):null,this.mergedBordered?l(`div`,{class:`${e}-input__state-border`}):null,this.showCount&&r===`textarea`?l(Q,null,{default:e=>{let{renderCount:t}=this;return t?t(e):o.count?.call(o,e)}}):null)}}),Fe=N(`n-grid`),Ie={span:{type:[Number,String],default:1},offset:{type:[Number,String],default:0},suffix:Boolean,privateOffset:Number,privateSpan:Number,privateColStart:Number,privateShow:{type:Boolean,default:!0}};j(Ie);var Le=C({__GRID_ITEM__:!0,name:`GridItem`,alias:[`Gi`],props:Ie,setup(){let{isSsrRef:e,xGapRef:t,itemStyleRef:n,overflowRef:r,layoutShiftDisabledRef:i}=c(Fe),a=h();return{overflow:r,itemStyle:n,layoutShiftDisabled:i,mergedXGap:v(()=>I(t.value||0)),deriveStyle:()=>{e.value;let{privateSpan:n=1,privateShow:r=!0,privateColStart:i=void 0,privateOffset:o=0}=a.vnode.props,{value:s}=t,c=I(s||0);return{display:r?``:`none`,gridColumn:`${i??`span ${n}`} / span ${n}`,marginLeft:o?`calc((100% - (${n} - 1) * ${c}) / ${n} * ${o} + ${c} * ${o})`:``}}}},render(){var e;if(this.layoutShiftDisabled){let{span:e,offset:t,mergedXGap:n}=this;return l(`div`,{style:{gridColumn:`span ${e} / span ${e}`,marginLeft:t?`calc((100% - (${e} - 1) * ${n}) / ${e} * ${t} + ${n} * ${t})`:``}},this.$slots)}return l(`div`,{style:[this.itemStyle,this.deriveStyle()]},(e=this.$slots).default?.call(e,{overflow:this.overflow}))}}),$={xs:0,s:640,m:1024,l:1280,xl:1536,xxl:1920},Re=24,ze=`__ssr__`,Be=C({name:`Grid`,inheritAttrs:!1,props:{layoutShiftDisabled:Boolean,responsive:{type:[String,Boolean],default:`self`},cols:{type:[Number,String],default:Re},itemResponsive:Boolean,collapsed:Boolean,collapsedRows:{type:Number,default:1},itemStyle:[Object,String],xGap:{type:[Number,String],default:0},yGap:{type:[Number,String],default:0}},setup(e){let{mergedClsPrefixRef:n,mergedBreakpointsRef:r}=T(e),i=/^\d+$/,a=f(void 0),o=me(r?.value||$),s=z(()=>!!(e.itemResponsive||!i.test(e.cols.toString())||!i.test(e.xGap.toString())||!i.test(e.yGap.toString()))),c=v(()=>{if(s.value)return e.responsive===`self`?a.value:o.value}),l=z(()=>Number(q(e.cols.toString(),c.value))??Re),d=z(()=>q(e.xGap.toString(),c.value)),p=z(()=>q(e.yGap.toString(),c.value)),h=e=>{a.value=e.contentRect.width},g=e=>{ce(h,e)},y=f(!1),b=v(()=>{if(e.responsive===`self`)return g}),x=f(!1),S=f();return u(()=>{let{value:e}=S;e&&e.hasAttribute(ze)&&(e.removeAttribute(ze),x.value=!0)}),m(Fe,{layoutShiftDisabledRef:_(e,`layoutShiftDisabled`),isSsrRef:x,itemStyleRef:_(e,`itemStyle`),xGapRef:d,overflowRef:y}),{isSsr:!t,contentEl:S,mergedClsPrefix:n,style:v(()=>e.layoutShiftDisabled?{width:`100%`,display:`grid`,gridTemplateColumns:`repeat(${e.cols}, minmax(0, 1fr))`,columnGap:I(e.xGap),rowGap:I(e.yGap)}:{width:`100%`,display:`grid`,gridTemplateColumns:`repeat(${l.value}, minmax(0, 1fr))`,columnGap:I(d.value),rowGap:I(p.value)}),isResponsive:s,responsiveQuery:c,responsiveCols:l,handleResize:b,overflow:y}},render(){if(this.layoutShiftDisabled)return l(`div`,b({ref:`contentEl`,class:`${this.mergedClsPrefix}-grid`,style:this.style},this.$attrs),this.$slots);let e=()=>{this.overflow=!1;let e=le(ge(this)),t=[],{collapsed:n,collapsedRows:r,responsiveCols:i,responsiveQuery:a}=this;e.forEach(e=>{if(e?.type?.__GRID_ITEM__!==!0)return;if(_e(e)){let n=y(e);n.props?n.props.privateShow=!1:n.props={privateShow:!1},t.push({child:n,rawChildSpan:0});return}e.dirs=e.dirs?.filter(({dir:e})=>e!==S)||null,e.dirs?.length===0&&(e.dirs=null);let n=y(e),r=Number(q(n.props?.span,a)??1);r!==0&&t.push({child:n,rawChildSpan:r})});let o=0,s=t[t.length-1]?.child;if(s?.props){let e=s.props?.suffix;e!==void 0&&e!==!1&&(o=Number(q(s.props?.span,a)??1),s.props.privateSpan=o,s.props.privateColStart=i+1-o,s.props.privateShow=s.props.privateShow??!0)}let c=0,u=!1;for(let{child:e,rawChildSpan:s}of t){if(u&&(this.overflow=!0),!u){let t=Number(q(e.props?.offset,a)??0),l=Math.min(s+t,i);if(e.props?(e.props.privateSpan=l,e.props.privateOffset=t):e.props={privateSpan:l,privateOffset:t},n){let e=c%i;l+e>i&&(c+=i-e),l+c+o>r*i?u=!0:c+=l}}u&&(e.props?e.props.privateShow!==!0&&(e.props.privateShow=!1):e.props={privateShow:!1})}return l(`div`,b({ref:`contentEl`,class:`${this.mergedClsPrefix}-grid`,style:this.style,[ze]:this.isSsr||void 0},this.$attrs),t.map(({child:e})=>e))};return this.isResponsive&&this.responsive===`self`?l(V,{onResize:this.handleResize},{default:e}):e()}});export{Ce as a,X as c,Ee as i,Y as l,Le as n,ve as o,Pe as r,ge as s,Be as t};