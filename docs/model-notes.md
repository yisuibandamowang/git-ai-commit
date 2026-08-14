# Model Notes

## Primary choice

- Ollama: qwen2.5-coder:7b
- Hugging Face: Qwen/Qwen2.5-Coder-7B-Instruct
- Reason: code-focused 7B model, easier to fine-tune and better aligned with commit-message generation.

## MoE experiment choice

- allenai/OLMoE-1B-7B-0125-Instruct-GGUF
- Reason: satisfies the MoE preference while staying around the 7B class and is usable locally through Ollama-compatible GGUF workflows.

## Recommendation

- Use qwen2.5-coder:7b for the plugin default through Ollama.
- Keep OLMoE as an optional experiment target for later comparison or fine-tuning trials.
