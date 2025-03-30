package com.vsdguzman;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

public class SuggestionsProvider implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        // Optionally, retrieve the source if needed:
        // ServerCommandSource source = context.getSource();

        builder.suggest("Arrow");
        builder.suggest("Normal");
        builder.suggest("Spiral");
        // Return the built suggestions.
        return builder.buildFuture();
    }
}
