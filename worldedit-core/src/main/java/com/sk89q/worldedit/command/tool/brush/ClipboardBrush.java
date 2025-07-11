/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.command.tool.brush;

import com.fastasyncworldedit.core.math.transform.MutatingOperationTransformHolder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;

import java.util.concurrent.ThreadLocalRandom;

public class ClipboardBrush implements Brush {

    private final ClipboardHolder holder;
    private final boolean ignoreAirBlocks;
    private final boolean usingOrigin;
    private final boolean pasteEntities;
    private final boolean pasteBiomes;
    private final Mask sourceMask;
    //FAWE start - random rotation
    private final boolean randomRotate;
    //FAWE end

    public ClipboardBrush(ClipboardHolder holder, boolean ignoreAirBlocks, boolean usingOrigin) {
        this.holder = holder;
        this.ignoreAirBlocks = ignoreAirBlocks;
        this.usingOrigin = usingOrigin;
        this.pasteBiomes = false;
        this.pasteEntities = false;
        this.sourceMask = null;
        //FAWE start - random rotation
        this.randomRotate = false;
        //FAWE end
    }

    public ClipboardBrush(
            ClipboardHolder holder, boolean ignoreAirBlocks, boolean usingOrigin, boolean pasteEntities,
            boolean pasteBiomes, Mask sourceMask
    ) {
        //FAWE start - random rotation
        this(holder, ignoreAirBlocks, usingOrigin, pasteEntities, pasteBiomes, sourceMask, false);
    }

    public ClipboardBrush(
            ClipboardHolder holder, boolean ignoreAirBlocks, boolean usingOrigin, boolean pasteEntities,
            boolean pasteBiomes, Mask sourceMask, boolean randomRotate
    ) {
        //FAWE end
        this.holder = holder;
        this.ignoreAirBlocks = ignoreAirBlocks;
        this.usingOrigin = usingOrigin;
        this.pasteEntities = pasteEntities;
        this.pasteBiomes = pasteBiomes;
        this.sourceMask = sourceMask;
        this.randomRotate = randomRotate;
    }

    @Override
    public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws
            MaxChangedBlocksException {
        //FAWE start - random rotation
        Transform originalTransform = holder.getTransform();
        Transform transform = new AffineTransform();
        if (this.randomRotate) {
            int rotate = 90 * ThreadLocalRandom.current().nextInt(4);
            transform = ((AffineTransform) transform).rotateY(rotate);
            if (originalTransform != null) {
                transform = originalTransform.combine(MutatingOperationTransformHolder.transform(originalTransform, true));
            }
        }
        holder.setTransform(transform);
        //FAWE end
        Clipboard clipboard = holder.getClipboard();
        Region region = clipboard.getRegion();
        BlockVector3 centerOffset = region.getCenter().toBlockPoint().subtract(clipboard.getOrigin());

        Operation operation = holder
                .createPaste(editSession)
                .to(usingOrigin ? position : position.subtract(centerOffset))
                .ignoreAirBlocks(ignoreAirBlocks)
                .copyEntities(pasteEntities)
                .copyBiomes(pasteBiomes)
                .maskSource(sourceMask)
                .build();

        Operations.completeLegacy(operation);
        //FAWE start - random rotation
        // reset transform
        holder.setTransform(originalTransform);
        //FAWE end
    }

}
